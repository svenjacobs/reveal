package com.svenjacobs.reveal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

public class RevealStateTest : BaseRevealTest() {

	@Test
	public fun stateContainsKeys() {
		test { _, revealState, _ ->
			assertTrue(revealState.revealableKeys.contains(Keys.Key1))
			assertTrue(revealState.revealableKeys.contains(Keys.Key2))
			assertFalse(revealState.revealableKeys.contains(Keys.Key3))
		}
	}
}
