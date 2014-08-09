package com.taskadapter.redmineapi.internal.comm;

import org.apache.http.conn.ClientConnectionManager;

import java.util.concurrent.TimeUnit;

public final class ConnectionEvictor implements Runnable {

	/**
	 * "Terminate" flag.
	 */
	private boolean terminate;

	/**
	 * Used connection manager.
	 */
	private final ClientConnectionManager connManager;

	/**
	 * Eviction interval.
	 */
	private final long evictionInverval;

	/**
	 * Idle connection timeout.
	 */
	private final int idleTimeout;

	public ConnectionEvictor(ClientConnectionManager connManager,
			int evictionInverval, int idleTimeout) {
		this.connManager = connManager;
		this.evictionInverval = evictionInverval * 1000L;
		this.idleTimeout = idleTimeout;
	}

	@Override
	public void run() {
		while (getNextEviction()) {
			connManager.closeExpiredConnections();
			connManager.closeIdleConnections(idleTimeout, TimeUnit.SECONDS);
		}
	}

	/**
	 * Waits for a next eviction.
	 */
	private boolean getNextEviction() {
		long nowTime = System.currentTimeMillis();
		final long sleepEndTime = nowTime + evictionInverval;

		while (nowTime < sleepEndTime) {
			final long toSleep = Math.max(100, sleepEndTime - nowTime);
			synchronized (this) {
				if (terminate)
					return false;
				try {
					wait(toSleep);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			nowTime = System.currentTimeMillis();
		}
		return true;
	}

	/**
	 * Shutdowns an evictor.
	 */
	public synchronized void shutdown() {
		terminate = true;
		notifyAll();
	}

}
