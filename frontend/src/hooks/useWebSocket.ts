// ─── Real-time WebSocket hook (STOMP over SockJS) ─────────
import { useEffect, useRef, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface UseWebSocketOptions {
  /** Called when STOMP is connected */
  onConnect?: () => void;
  /** Called on connection error */
  onError?: (err: unknown) => void;
}

interface UseWebSocketReturn {
  /** Subscribe to a STOMP topic. Returns an unsubscribe function. */
  subscribe: (topic: string, handler: (payload: unknown) => void) => () => void;
  /** Whether the STOMP client is currently connected */
  connected: boolean;
}

let _client: Client | null = null;
let _refCount = 0;

/**
 * Singleton STOMP client shared across all hook instances so only one
 * WebSocket connection is opened per app session.
 */
export function useWebSocket(options: UseWebSocketOptions = {}): UseWebSocketReturn {
  const connectedRef = useRef(false);
  const { onConnect, onError } = options;

  useEffect(() => {
    _refCount++;

    if (!_client) {
      _client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        reconnectDelay: 5000,
        onConnect: () => {
          connectedRef.current = true;
          onConnect?.();
        },
        onStompError: (frame) => {
          console.error('[WS] STOMP error', frame);
          onError?.(frame);
        },
        onDisconnect: () => {
          connectedRef.current = false;
        },
      });
      _client.activate();
    }

    return () => {
      _refCount--;
      if (_refCount === 0 && _client) {
        _client.deactivate();
        _client = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const subscribe = useCallback(
    (topic: string, handler: (payload: unknown) => void): (() => void) => {
      if (!_client) return () => {};

      let sub: StompSubscription | null = null;

      const doSubscribe = () => {
        if (!_client?.connected) return;
        sub = _client.subscribe(topic, (msg: IMessage) => {
          try {
            handler(JSON.parse(msg.body));
          } catch {
            handler(msg.body);
          }
        });
      };

      if (_client.connected) {
        doSubscribe();
      } else {
        // Queue subscription until connected
        const prev = _client.onConnect;
        _client.onConnect = (frame) => {
          prev?.call(_client, frame);
          doSubscribe();
        };
      }

      return () => {
        sub?.unsubscribe();
      };
    },
    []
  );

  return { subscribe, connected: connectedRef.current };
}
