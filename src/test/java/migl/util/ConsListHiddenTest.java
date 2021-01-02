
package migl.util;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsListHiddenTest {

	private ConsList<String> list;

	@BeforeEach
	void init() {
		list = ConsListFactory.nil();
	}

	@Test
	void testPrependHidden() {
		assertEquals(0, list.size());
		list = list.prepend("1");
		assertEquals(1, list.size());
		assertEquals("1", list.car());
		list = list.prepend("2");
		assertEquals(2, list.size());
		assertEquals("2", list.car());
		list = list.prepend("3");
		assertEquals(3, list.size());
		assertEquals("3", list.car());
	}

	@Test
	void testPrependSharesSublistHidden() {
		ConsList<String> list2 = list.prepend("2");
		assertEquals(list, list2.cdr());
		assertSame(list, list2.cdr());
		ConsList<String> list3 = list2.prepend("1");
		assertEquals(list2, list3.cdr());
		assertSame(list2, list3.cdr());
	}

	@Test
	void testAppendHidden() {
		assertEquals(0, list.size());
		list = list.append("1");
		assertEquals(1, list.size());
		assertEquals("1", list.car());
		list = list.append("2");
		assertEquals(2, list.size());
		assertEquals("1", list.car());
		assertEquals("2", list.cdr().car());
		list = list.append("3");
		assertEquals(3, list.size());
		assertEquals("1", list.car());
		assertEquals("2", list.cdr().car());
		assertEquals("3", list.cdr().cdr().car());
	}

	@Test
	void testAppendDoesNoShareSublistHidden() {
		ConsList<String> list2 = list.append("1");
		assertEquals(list, list2.cdr());
		ConsList<String> list3 = list2.append("2");
		assertEquals(list2.car(), list3.car());
		assertNotSame(list2, list3.cdr());
	}

	@Test
	void testToStringEmptyHidden() {
		assertEquals("()", list.toString());
	}

	@Test
	void testToStringSingletonHidden() {
		list = list.prepend("1");
		assertEquals("(1)", list.toString());
	}

	@Test
	void testToStringTwoElementsHidden() {
		list = list.prepend("2");
		list = list.prepend("1");
		assertEquals("(1 2)", list.toString());
	}

	@Test
	void testEqualsToArrayHidden() {
		list = list.append("1");
		Object[] liste = { "1" };
		assertEquals(liste[0], list.toArray()[0]);

	}

	@Test
	void testEqualsReduceHidden() {
		list = list.append("1");
		list = list.append("2");
		list = list.append("3");
		list = list.append("4");
		assertEquals("1234", list.reduce("", String::concat));
	}

	@Test
	void testEfficiencyOfPrependHidden() {
		assertTimeout(ofSeconds(10), () -> {
			for (int i = 0; i < 1000; i++) {
				list = list.prepend(Integer.toString(i));
			}
		});
	}

	@Test
	void testEfficiencyOfSizeHidden() {
		assertTimeout(ofSeconds(10), () -> {
			for (int i = 1; i <= 1000; i++) {
				list = list.prepend(Integer.toString(i));
				assertEquals(i, list.size());
			}
		});
	}

	@Test
	void testEmptyVarArgsHidden() {
		ConsList<String> l1 = ConsListFactory.asList();
		assertEquals(ConsListFactory.nil(), l1);
	}

	@Test
	void testBreakerForOneStudentHidden() {
		ConsList<Integer> l1 = ConsListFactory.nil();
		l1 = l1.prepend(2);
		assertEquals(1, l1.size());
	}

	@Test
	void testEvilNullCaseHidden() {
		ConsList<Integer> l1 = ConsListFactory.nil();
		l1 = l1.prepend(null);
		assertEquals(1, l1.size());
		assertEquals("(null)", l1.toString());
	}

	@Test
	void testEvilNullCaseVandewoordeHidden() {
		ConsList<Integer> l1 = ConsListFactory.nil();
		l1 = l1.prepend(3).prepend(null).prepend(null);
		assertEquals(3, l1.size());
		assertEquals("(null null 3)", l1.toString());
	}

	@Test
	void testPreventUsingCollectionHidden() throws IllegalArgumentException, IllegalAccessException {
		ConsList<String> l1 = ConsListFactory.asList("a", "b", "c");
		Class<?> clazz = l1.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (Collection.class.isAssignableFrom(field.get(l1).getClass())) {
				fail("One should not delegate to a Collection object all the work");
			}
		}
		if (Collection.class.isAssignableFrom(clazz)) {
			fail("One should not inherit from a Collection all the work");
		}
	}

	@Test
	void testThatConsListUseConsPouillyHidden() throws IllegalArgumentException, IllegalAccessException {
		ConsList<String> l1 = ConsListFactory.asList("a", "b", "c");
		Class<?> clazz = l1.getClass();
		boolean consFound = false;
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (Cons.class.isAssignableFrom(field.get(l1).getClass())) {
				consFound = true;
			}
		}
		if (!consFound && Cons.class.isAssignableFrom(clazz)) {
			consFound = true;
		}
		assertTrue(consFound, "One should use the Cons data structure for this exercise");
	}

	@Test
	void testThatIteratorIsOwnMadeHidden() {
		ConsList<String> l1 = ConsListFactory.asList("a", "b", "c");
		Iterator<String> it = l1.iterator();
		List<String> l2 = new ArrayList<>();
		l2.add("a");
		l2.add("b");
		l2.add("c");
		List<String> l3 = new LinkedList<>(l2);
		List<String> l4 = new Vector<>(l2);
		List<?>[] lists = { l2, l3, l4 };
		for (List<?> l : lists) {
			assertNotEquals(it.getClass(), l.iterator().getClass(),
					"One should use it's own iterator implementation for this exercise");
		}
	}
}
