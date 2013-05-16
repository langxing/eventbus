
package sx.richard.eventbus;

/** Used for listening to incoming events
 * @author Richard Taylor */
public interface EventListener<T extends Object> {
	
	/** Invoked when an event {@link Object} is received, of a specific type
	 * @param event */
	public void onEvent (T event);
	
}
