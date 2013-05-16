
package sx.richard.eventbus;

/** Manages the execution of tasks on the appropriate thread. The
 * {@link ThreadPreference} can be used when registering a subscriber with
 * {@link EventBus}; and must be created on the target thread. When an event is
 * posted, this will check whether this it has been posted on the same thread.
 * If it has, it will be executed immediately. Otherwise, a task will be posted
 * to the {@link ThreadExecutor} used for this {@link ThreadPreference}.
 * @author Richard Taylor */
public final class ThreadPreference {
	
	/** Used to submit tasks for execution on the desired thread
	 * @author Richard Taylor */
	public interface ThreadExecutor {
		
		/** Invoked when an event should be submitted for execution on the desired
		 * thread. This <b>must</b> call {@link EventListener#onEvent(Object)}
		 * @param event the event
		 * @param listener the {@link EventListener} */
		public void post (Object event, EventListener<Object> listener);
	}
	
	private final ThreadExecutor executor;
	private final Thread thread;
	
	/** @param executor the {@link ThreadExecutor} that will be invoked from the
	 *           event-source's thread (unless the threads match) */
	public ThreadPreference (ThreadExecutor executor) {
		if (executor == null)
			throw new NullPointerException("ThreadExecutor must not be null");
		this.executor = executor;
		thread = Thread.currentThread();
	}
	
	/** @return the {@link ThreadExecutor} */
	public ThreadExecutor getExecutor () {
		return executor;
	}
	
	/** @return whether this thread is the one the {@link ThreadPreference} was
	 *         created on */
	public boolean isThis () {
		return Thread.currentThread() == thread;
	}
	
}
