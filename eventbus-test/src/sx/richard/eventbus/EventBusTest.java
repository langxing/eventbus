
package sx.richard.eventbus;

import sx.richard.eventbus.ThreadPreference.ThreadExecutor;

/** Tests the functionality of {@link EventBus}
 * @author Richard Taylor */
public class EventBusTest {
	
	public static class TestEvent1 {}
	
	public static class TestEvent2 {}
	
	public static void main (String[] args) {
		new EventBusTest();
	}
	
	private EventBus eventBus;
	private int invokeCount;
	private boolean threadedPost;
	private boolean threadStarted;
	
	public EventBusTest () {
		testSingleEvent();
		testTwoEvents();
		testThreadedEvent();
		System.out.println("Success");
	}
	
	private ThreadPreference createThreadPreference () {
		return new ThreadPreference(new ThreadExecutor() {
			
			@Override
			public void post (Object event, EventListener<Object> listener) {
				threadedPost = true;
				listener.onEvent(event);
			}
			
		});
	}
	
	private <T extends Object> EventListener<T> invokeCounter (Class<T> clazz) {
		return new EventListener<T>() {
			
			@Override
			public void onEvent (T event) {
				invokeCount++;
			}
			
		};
	}
	
	private void testSingleEvent () {
		eventBus = new EventBus();
		invokeCount = 0;
		eventBus.register(this, TestEvent1.class, invokeCounter(TestEvent1.class));
		eventBus.post(TestEvent1.class);
		if (invokeCount != 1)
			throw new AssertionError();
		
		eventBus.post(new TestEvent1());
		if (invokeCount != 2)
			throw new AssertionError();
		
		eventBus.unregister(this);
		eventBus.post(TestEvent1.class);
		if (invokeCount != 2)
			throw new AssertionError();
	}
	
	private void testThreadedEvent () {
		eventBus = new EventBus();
		invokeCount = 0;
		threadedPost = false;
		ThreadPreference main = createThreadPreference();
		eventBus.register(this, main, TestEvent1.class, invokeCounter(TestEvent1.class));
		eventBus.post(TestEvent1.class);
		if (invokeCount != 1)
			throw new AssertionError();
		if (threadedPost)
			throw new AssertionError();
		
		threadStarted = false;
		new Thread(new Runnable() {
			
			@Override
			public void run () {
				eventBus.register(this, createThreadPreference(), TestEvent1.class, invokeCounter(TestEvent1.class));
				threadStarted = true;
			}
		}).start();
		while (!threadStarted) {
			Thread.yield();
		}
		
		invokeCount = 0;
		eventBus.post(TestEvent1.class);
		long failTime = System.currentTimeMillis() + 500;
		while (!threadedPost) {
			Thread.yield();
			if (System.currentTimeMillis() > failTime)
				throw new AssertionError();
		}
		
		if (invokeCount != 2)
			throw new AssertionError();
	}
	
	private void testTwoEvents () {
		eventBus = new EventBus();
		invokeCount = 0;
		eventBus.register(this, TestEvent1.class, invokeCounter(TestEvent1.class));
		eventBus.register(this, TestEvent2.class, invokeCounter(TestEvent2.class));
		eventBus.post(TestEvent1.class);
		eventBus.post(TestEvent2.class);
		if (invokeCount != 2)
			throw new AssertionError();
		
		eventBus.unregister(this, TestEvent1.class);
		eventBus.post(TestEvent1.class);
		eventBus.post(TestEvent2.class);
		if (invokeCount != 3)
			throw new AssertionError();
	}
}
