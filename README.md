EventBus
========

Type-safe and thread-safe, one-to-many event bus in Java, optimized for speed and low-garbage on Android. 

Event classes can be posted to any object that is registered to receive that particular type. Any registered received can specify a preferred thread to execute the response on, and any number of receivers and objects may register for a given event type.

Available under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

## Basic usage
```java
public class Demo {
  
	class SomethingChanged {
		String content;
	}
	
	public Demo () {
		// Create the EventBus
		EventBus eventBus = new EventBus();
		// Register subscribers
		eventBus.register(this, SomethingChanged.class, somethingChanged());
		// Post an event
		SomethingChanged event = new SomethingChanged();
		event.content = "Hello world!";
		eventBus.post(event);
	}
	
	private EventListener<SomethingChanged> somethingChanged () {
		return new EventListener<SomethingChanged>() {
			
			@Override
			public void onEvent (SomethingChanged event) {
				System.out.println("content=" + event.content);
			}
			
		};
	}
	
}
```

## Multi-threaded usage (Android)
```java
public class Model {
  
	public static EventBus eventBus = new EventBus();
	
	public class WorkComplete { }
	
	public void runTaskInBackground() {
		// Do some work on this background thread
		eventBus.post(new WorkComplete());
	}
	
}

public class Demo extends Activity {
	
	@Override
	public void onPause() {
		super.onPause();
		eventBus.unregister(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		eventBus.register(this, createThreadPreference(), WorkComplete.class, workComplete());
	}
	
	private EventListener<WorkComplete> workComplete() {
		return new EventListener<WorkComplete>() {

			@Override
			public void onEvent (WorkComplete event) {
				// This will execute on the UI thread
			}
			
		};
	}
	
	private ThreadPreference createThreadPreference() {
		return new ThreadPreference(new ThreadExecutor() {

			@Override
			public void post (final Object event, final EventListener<Object> listener) {
				// This is invoked on the event-source's thread
				runOnUiThread(new Runnable() { public void run() {
					// Invoke the listener ourselves, on the Activity's UI thread
					listener.onEvent(event);
				}});
			}
			
		});
	}
	
}
```
