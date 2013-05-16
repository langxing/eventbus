
package sx.richard.eventbus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Maintains the list of {@link Subscription}s, and a map of registered objects
 * against them. Keeping both collections allows manipulation without creating
 * garbage, and optimizes for {@link Subscribers#post(Object)}.ad.
 * @author Richard Taylor */
class Subscribers {
	
	private final Map<Object, List<Subscription>> objectSubscriptions;
	private final List<Subscription> subscriptions;
	
	public Subscribers () {
		objectSubscriptions = Collections.synchronizedMap(new HashMap<Object, List<Subscription>>());
		subscriptions = new ArrayList<Subscription>();
	}
	
	private List<Subscription> getSubscriptions (Object object, boolean create) {
		List<Subscription> listeners;
		if (create) {
			synchronized (objectSubscriptions) {
				listeners = objectSubscriptions.get(object);
				if (listeners == null && create) {
					listeners = new ArrayList<Subscription>(1);
					objectSubscriptions.put(object, listeners);
				}
			}
		} else {
			listeners = objectSubscriptions.get(object);
		}
		return listeners;
	}
	
	/** Posts an event to all the {@link Subscription}s
	 * @param event the event to post */
	@SuppressWarnings("unchecked")
	// Type-safety maintained by logic
	public void post (Object event) {
		for (int i = 0, n = subscriptions.size(); i < n; i++) {
			Subscription subscription = subscriptions.get(i);
			ThreadPreference threadPreference = subscription.threadPreference;
			EventListener<Object> listener = (EventListener<Object>)subscription.listener;
			if (threadPreference == null || threadPreference.isThis()) {
				listener.onEvent(event);
			} else {
				threadPreference.getExecutor().post(event, listener);
			}
		}
	}
	
	/** Registers an object and an {@link EventListener}
	 * @param object the object to associate with the listener
	 * @param threadPreference the {@link ThreadPreference}, may be
	 *           <code>null</code>
	 * @param listener the {@link EventListener} */
	public void register (Object object, ThreadPreference threadPreference, EventListener<?> listener) {
		List<Subscription> subscribers = getSubscriptions(object, true);
		if (subscribers.contains(listener))
			throw new RuntimeException("EventListener already exists for this Object");
		Subscription subscriber = new Subscription(threadPreference, listener);
		subscribers.add(subscriber);
		subscriptions.add(subscriber);
	}
	
	/** Unregsiters all the {@link EventListener}s associated with one
	 * {@link Object}
	 * @param object the {@link Object} */
	public void unregister (Object object) {
		synchronized (objectSubscriptions) {
			List<Subscription> subscriptions = getSubscriptions(object, false);
			if (subscriptions != null) {
				objectSubscriptions.remove(object);
				this.subscriptions.removeAll(subscriptions);
			}
		}
	}
	
}