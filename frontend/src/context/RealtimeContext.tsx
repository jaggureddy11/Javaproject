import React, { createContext, useContext, useEffect, useState, useRef, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { RealtimeEvent, RealtimeConnectionState } from '../types/realtime';

interface RealtimeContextType {
  connectionState: RealtimeConnectionState;
  subscribe: (topic: string, handler: (event: RealtimeEvent) => void) => () => void;
  registerResyncHandler: (handler: () => void) => () => void;
}

const RealtimeContext = createContext<RealtimeContextType | null>(null);

const PROCESSED_EVENT_LIMIT = 500;

export const RealtimeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [connectionState, setConnectionState] = useState<RealtimeConnectionState>('CONNECTING');
  const clientRef = useRef<Client | null>(null);
  const reconnectAttemptRef = useRef<number>(0);
  const processedEventsRef = useRef<Set<string>>(new Set());
  const lastSequenceRef = useRef<number>(0);
  const resyncHandlersRef = useRef<Set<() => void>>(new Set());

  const getReconnectDelay = (attempt: number): number => {
    const delays = [1000, 2000, 4000, 8000, 16000, 30000];
    return delays[Math.min(attempt, delays.length - 1)];
  };

  const registerResyncHandler = useCallback((handler: () => void) => {
    resyncHandlersRef.current.add(handler);
    return () => {
      resyncHandlersRef.current.delete(handler);
    };
  }, []);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: getReconnectDelay(reconnectAttemptRef.current),
      onConnect: () => {
        setConnectionState('CONNECTED');
        const wasReconnecting = reconnectAttemptRef.current > 0;
        reconnectAttemptRef.current = 0;

        if (wasReconnecting) {
          console.log('[Realtime] Reconnected — triggering REST resync');
          resyncHandlersRef.current.forEach((fn) => fn());
        }
      },
      onStompError: (frame) => {
        console.error('[Realtime] STOMP Error:', frame);
        setConnectionState('ERROR');
      },
      onWebSocketClose: () => {
        reconnectAttemptRef.current++;
        setConnectionState(reconnectAttemptRef.current > 1 ? 'RECONNECTING' : 'DISCONNECTED');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const subscribe = useCallback((topic: string, handler: (event: RealtimeEvent) => void): (() => void) => {
    if (!clientRef.current) return () => {};

    let sub: StompSubscription | null = null;

    const doSubscribe = () => {
      if (!clientRef.current?.connected) return;
      sub = clientRef.current.subscribe(topic, (msg: IMessage) => {
        try {
          const event: RealtimeEvent = JSON.parse(msg.body);
          if (event && event.eventId) {
            // Deduplication check
            if (processedEventsRef.current.has(event.eventId)) {
              return; // Ignore duplicate frame
            }
            processedEventsRef.current.add(event.eventId);
            if (processedEventsRef.current.size > PROCESSED_EVENT_LIMIT) {
              const firstKey = processedEventsRef.current.values().next().value;
              if (firstKey) processedEventsRef.current.delete(firstKey);
            }

            // Sequence ordering check
            if (event.sequence && event.sequence < lastSequenceRef.current) {
              console.warn(`[Realtime] Out-of-order event dropped: seq=${event.sequence} < last=${lastSequenceRef.current}`);
              return;
            }
            if (event.sequence) {
              lastSequenceRef.current = event.sequence;
            }
          }
          handler(event);
        } catch {
          handler({ eventId: Math.random().toString(), eventType: 'OPTIMIZATION_PROGRESS', payload: msg.body });
        }
      });
    };

    if (clientRef.current.connected) {
      doSubscribe();
    } else {
      const prevOnConnect = clientRef.current.onConnect;
      clientRef.current.onConnect = (frame) => {
        prevOnConnect?.call(clientRef.current, frame);
        doSubscribe();
      };
    }

    return () => {
      sub?.unsubscribe();
    };
  }, []);

  return (
    <RealtimeContext.Provider value={{ connectionState, subscribe, registerResyncHandler }}>
      {children}
    </RealtimeContext.Provider>
  );
};

export const useRealtime = () => {
  const ctx = useContext(RealtimeContext);
  if (!ctx) {
    throw new Error('useRealtime must be used within a RealtimeProvider');
  }
  return ctx;
};
