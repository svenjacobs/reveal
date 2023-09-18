package com.svenjacobs.reveal

/**
 * Key to be used with [RevealScope.revealable].
 *
 * Must be unique per [RevealState] instance. `enum`, `object` or `String` are recommended as keys.
 *
 * If a custom type is used, proper structural equality (`==`) must be ensured by implementing the
 * `equals` operator. Failing to do so might lead to unexpected behaviour.
 *
 * @see RevealScope.revealable
 */
public typealias Key = Any
