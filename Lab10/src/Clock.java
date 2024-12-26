import javax.swing.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
interface Observer
{
    void update(LocalTime currentTime);
}

class Clock extends Timer
{
    private LocalTime currentTime;
    private final List<Observer> observers;

    public Clock()
    {
        super(1000, null);
        this.currentTime = LocalTime.now();
        this.observers = new ArrayList<>();

        this.addActionListener(e ->
        {
            currentTime = LocalTime.now();
            notifyObservers();
        });
    }

    public void addObserver(Observer observer)
    {
        observers.add(observer);
    }

    private void notifyObservers()
    {
        for (Observer observer : observers)
        {
            observer.update(currentTime);
        }
    }
}

class Alarm implements Observer
{
    private final LocalTime alarmTime;
    private boolean triggered = false;
    private final Runnable onTrigger;

    public Alarm(LocalTime alarmTime, Runnable onTrigger)
    {
        this.alarmTime = alarmTime;
        this.onTrigger = onTrigger;
    }

    public LocalTime getAlarmTime()
    {
        return alarmTime;
    }

    public boolean isTriggered()
    {
        return triggered;
    }

    @Override
    public void update(LocalTime currentTime)
    {
        if (!triggered && currentTime.equals(alarmTime))
        {
            triggered = true;
            onTrigger.run();
        }
    }
}
