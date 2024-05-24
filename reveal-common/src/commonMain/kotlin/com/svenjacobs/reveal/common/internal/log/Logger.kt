package com.svenjacobs.reveal.common.internal.log

/**
 * Minimal logging adapter for development purposes.
 * For internal use only.
 */
public object Logger {

	public fun interface Adapter {
		public fun d(message: String, tag: String)
	}

	public var adapter: Adapter? = null

	public fun d(message: String, tag: String = "Reveal") {
		adapter?.d(message, tag)
	}
}
