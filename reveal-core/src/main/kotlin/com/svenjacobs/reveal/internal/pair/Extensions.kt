package com.svenjacobs.reveal.internal.pair

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Only calls [body] and returns its result if both values of [Pair] are non-null. If any of the
 * values are `null`, function returns `null`.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <A : Any, B : Any, R> Pair<A?, B?>.safe(body: (A, B) -> R): R? {
	contract {
		callsInPlace(body, InvocationKind.AT_MOST_ONCE)
	}
	return if (first == null || second == null) {
		null
	} else {
		body(first!!, second!!)
	}
}
