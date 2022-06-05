package scheduler.interfaces;

import javafx.scene.Scene;
import scheduler.Main;

import java.util.function.Consumer;

/**
 * Used by scene controllers to bring information back to the previous scene.
 * @param <T> The type of information. (IE: Appointment, Customer)
 */
public abstract class BackTracker<T> {
    /**
     * The scene to go back to.
     */
    protected Scene backScene;

    /**
     * The function to call just before going back.
     */
    private Consumer<T> callback;

    /**
     * Tells the backtracker where to go back to.
     *
     * This call will not set a callback function to bring information back to the previous scene.
     * @param from The scene to go back to.
     */
    public void setFrom(Scene from){
        setFrom(from,null);
    }

    /**
     * Tells the backtracker where to go back to.
     *
     * A callback is provided by the previous scene to process information upon return.
     * @param from The scene to go back to.
     * @param callback The callback consumer function. Takes type T as a parameter.
     */
    public void setFrom(Scene from, Consumer<T> callback){
        backScene=from;
        this.callback=callback;
    }

    /**
     * Goes back to the previous scene if one had been provided.
     *
     * Will also call the previous scene's callback with the selected object.
     * @param obj Object to pass back to the previous scene, processed by the callback.
     */
    public void goBack(T obj){
        if(callback!=null)
            callback.accept(obj);
        if(backScene!=null)
            Main.setScene(backScene);
        backScene=null;
        callback=null;
    }

    /**
     * Goes back to the previous scene if one had been provided.
     *
     * Does not call the callback function. Useful for cancel-scenarios.
     */
    public void goBack(){
        if(backScene!=null)
            Main.setScene(backScene);
        backScene=null;
        callback=null;
    }
}
