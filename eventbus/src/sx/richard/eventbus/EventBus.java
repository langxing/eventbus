
package sx.richard.eventbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** The {@link EventBus} maintains a collection of {@link EventListener}s and an
 * {@link Object} they are associated with. Any object can be posted as an
 * event, and any object can associate itself with an {@link EventListener}.
 * Care is required in correctly managing your registered objects, you must
 * ensure that they are unregistered when no longer needed.
 * <p>
 * Registered {@link EventListener} may use a {@link ThreadPreference} to force
 * execution on a specific thread.
 * @author Richard Taylor */
public class EventBus {
	
	private final Map<Class<? extends Object>, Subscribers> eventSubscribers;
	
	public EventBus () {
		eventSubscribers = Collections.synchronizedMap(new HashMap<Class<? extends Object>, Subscribers>());
	}
	
	private Subscribers getSubscribers (Class<? extends Object> eventClass, boolean create) {
		Subscribers subscribers;
		if (create) {
			synchronized (eventSubscribers) {
				subscribers = eventSubscribers.get(eventClass);
				if (subscribers == null && create) {
					subscribers = new Subscribers();
					eventSubscribers.put(eventClass, subscribers);
				}
			}
		} else {
			subscribers = eventSubscribers.get(eventClass);
		}
		return subscribers;
	}
	
	/** Posts an event, a convenience method that will create an instance of the
	 * {@link Object} -- assuming it has an appropriate no-arg constructor
	 * @param eventClass the objects {@link Class} */
	public void post (Class<? extends Object> eventClass) {
		try {
			post(eventClass.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Posts an object to all subscribers
	 * @param event the {@link Object} to post */
	public void post (Object event) {
		Class<? extends Object> eventClass = event.getClass();
		Subscribers subscribers = getSubscribers(eventClass, false);
		if (subscribers != null) {
			subscribers.post(event);
		}
	}
	
	/** Uses a <code>null</code> {@link ThreadPreference}
	 * @see EventBus#register(Object, ThreadPreference, Class, EventListener) */
	public <T extends Object> void register (Object object, Class<T> eventClass, EventListener<T> listener) {
		register(object, null, eventClass, listener);
	}
	
	/** Registers an object and an {@link EventListener} (who should not be the
	 * same) to listen to an event of a given class
	 * @param object the object
	 * @param threadPreference the {@link ThreadPreference}, or <code>null</code>
	 *           for any thread
	 * @param eventClass the {@link Class} of the {@link Object} to listen out
	 *           for
	 * @param listener the {@link EventListener} */
	public <T extends Object> void register (Object object, ThreadPreference threadPreference, Class<T> eventClass,
		EventListener<T> listener) {
		if (object == null)
			throw new NullPointerException("Object must not be null");
		if (eventClass == null)
			throw new NullPointerException("EventClass must not be null");
		if (listener == null)
			throw new NullPointerException("EventListener must not be null");
		if (object == listener)
			throw new IllegalArgumentException("Object must not be the EventListener");
		Subscribers subscribers = getSubscribers(eventClass, true);
		subscribers.register(object, threadPreference, listener);
	}
	
	/** Unregisters all {@link EventListener}s associated with a given
	 * {@link Object}
	 * @param object the object */
	public void unregister (Object object) {
		synchronized (eventSubscribers) {
			for (Subscribers subscribers : eventSubscribers.values()) {
				subscribers.unregister(object);
			}
		}
	}
	
	/** Unregisters all {@link EventListener}s associated with a given
	 * {@link Object} that are listening for a particular {@link Class}
	 * @param object the {@link Object}
	 * @param eventClass the {@link Class} of the event object */
	public void unregister (Object object, Class<? extends Object> eventClass) {
		Subscribers subscribers = getSubscribers(eventClass, false);
		if (subscribers != null) {
			subscribers.unregister(object);
		}
	}
	
}
