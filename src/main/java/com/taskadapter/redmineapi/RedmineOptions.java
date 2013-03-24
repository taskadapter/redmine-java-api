package com.taskadapter.redmineapi;

/**
 * Connection pooling options.
 * 
 * @author maxkar
 * 
 */
public final class RedmineOptions {
	private final int maxOpen;
	private final int idleTimeout;
	private final int evictionCheck;

	/**
	 * Creates a new pooling config.
	 * 
	 * @param maxOpen
	 *            number of open connections.
	 * @param idleTimeout
	 *            idle timeout in seconds.
	 * @param evictionCheck
	 *            timeout between evictions checks in seconds.
	 */
	public RedmineOptions(int maxOpen, int idleTimeout,
			int evictionCheck) {
		this.maxOpen = maxOpen;
		this.idleTimeout = idleTimeout;
		this.evictionCheck = evictionCheck;
	}

	public static RedmineOptions simpleOptions() {
		return new RedmineOptions(Integer.MAX_VALUE, 30, 30);
	}

	public static RedmineOptions withMaxConnections(int maxOpen) {
		return new RedmineOptions(maxOpen, 30, 30);
	}

	public int getMaxOpenConnections() {
		return maxOpen;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public int getEvictionCheckInterval() {
		return evictionCheck;
	}

}
