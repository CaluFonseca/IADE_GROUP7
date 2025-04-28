package com.badlogic.UniverseConqueror.Utils;

public class Timer {

    private float time;       // Current time of the timer
    private float duration;   // Duration we want to count down
    private boolean running;  // Is the timer running or stopped

    // Constructor to initialize the timer with a specific duration
    public Timer(float duration) {
        this.duration = duration;  // Set the desired duration
        this.time = 0;             // Initialize the current time to zero
        this.running = false;      // Timer is initially stopped
    }

    // Start the timer, resetting the time to 0
    public void start() {
        running = true;  // Set the timer as running
        time = 0;        // Reset the time to 0
    }

    // Stop the timer
    public void stop() {
        running = false; // Set the timer as stopped
    }

    // Reset the timer time to 0 without affecting the duration
    public void reset() {
        time = 0;  // Reset the current time to zero
    }

    // Update the timer based on the delta time (time passed in each frame)
    public void update(float delta) {
        if (!running) return;  // If the timer is not running, do nothing

        time += delta;  // Increment the time by the delta (time passed)
        if (time >= duration) {
            running = false; // Stop the timer once the duration is reached
        }
    }

    // Check if the timer has finished (duration is reached or exceeded)
    public boolean isFinished() {
        return !running && time >= duration;  // Returns true if the timer is not running and the time has exceeded or reached the duration
    }

    // Check if the timer is still running
    public boolean isRunning() {
        return running;  // Returns true if the timer is running
    }

    // Get the current time of the timer
    public float getTime() {
        return time;  // Return the current time
    }

    // Get the duration of the timer
    public float getDuration() {
        return duration;  // Return the duration of the timer
    }

    // Set a new duration for the timer
    public void setDuration(float duration) {
        this.duration = duration;  // Set a new duration for the timer
    }
}
