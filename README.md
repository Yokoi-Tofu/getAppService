#getAppService

It is the library to get the app name that is displayed at the top.

## Description
Since the top app in the service startup can be acquired ,
You can make a program like something is executed when a particular application is started .

## How to use
###Add to gradle

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'jp.yokolabo:getapp:0.8'
}
```

###Generation and initialization of instance

```java
public class MyService extends Service{
	ApplicationManager am;
	String topApp;
   	String oldApp;
	Timer appTimer;
	
	@Override
    public void onCreate() {
		am = new ApplicationManager(this);	 //Instantiation of the class of this library
		topApp = "";						 //Application name at the top
		oldApp = "";						 //Application name who was on top just before
		appTimer = new Timer(true);			 //Timer gets the state of the regular application
	}
```

###Try out

```java
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        appTimer = new Timer(true);
        //750 Get the state of the application at millisecond intervals
        appTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Get the application that is on the top
                        topApp = am.getTopApp(oldApp);
                        //Replace the top of the app
                        oldApp = topApp;
                    }
                });
            }
        }, 1000, 750);
    }
```

###Example of use
Show toast when the Google Chrome has been initiated

```Java
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        appTimer = new Timer(true);
        //750 Get the state of the application at millisecond intervals
        appTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Get the application that is on the top
                        topApp = am.getTopApp(oldApp);
                        if(!topApp.equals(oldApp) && topApp.equals("Chrome")){
                            showText("Launch to google chrome");
                        }
                        //Replace the top of the app
                        oldApp = topApp;
                    }
                });
            }
        }, 1000, 750);
    }
```
