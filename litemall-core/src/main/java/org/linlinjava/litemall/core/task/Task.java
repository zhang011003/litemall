package org.linlinjava.litemall.core.task;

import com.google.common.primitives.Ints;

import java.time.LocalDateTime;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class Task implements Delayed, Runnable{
    private String id = "";
    private long start = 0;
    protected boolean needReenterQueue = false;
    private long delayInMilliseconds;
    public Task(String id, long delayInMilliseconds){
        this.id = id;
        this.delayInMilliseconds = delayInMilliseconds;
        this.start = System.currentTimeMillis() + delayInMilliseconds;
    }

    public String getId() {
        return id;
    }

    public boolean needReenterQueue() {
        return needReenterQueue;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = this.start - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    public final void reset() {
        this.start = System.currentTimeMillis() + this.delayInMilliseconds;
        this.needReenterQueue = false;
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(this.start - ((Task) o).start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Task)) {
            return false;
        }
        Task t = (Task)o;
        return this.id.equals(t.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                '}';
    }
}
