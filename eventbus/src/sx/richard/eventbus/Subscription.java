
package sx.richard.eventbus;

/** Represents the subscription of one {@link EventListener} and its
 * {@link ThreadPreference}
 * @author Richard Taylor */
class Subscription {
	
	/** The {@link EventListener} to invoke */
	public final EventListener<? extends Object> listener;
	/** The {@link ThreadPreference} for this subscription, may be
	 * <code>null</code> (indicates any thread) */
	public final ThreadPreference threadPreference;
	
	public Subscription (ThreadPreference threadPreference, EventListener<? extends Object> listener) {
		this.threadPreference = threadPreference;
		this.listener = listener;
	}
	
}
