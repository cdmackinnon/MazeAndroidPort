package com.example.amazebyconnormackinnon.generation;
/**
 * Specifies functionality of a maze factory that produces
 * a maze with a background thread.
 * It applies the abstract factory pattern. This class is the 
 * factory interface and MazeConfiguration is the product 
 * interface.
 * The asynchronous production implies that there is time 
 * between acceptance of an order (which returns immediately) 
 * and the actual delivery of a product. 
 * The order specifies what exactly is wanted plus 
 * a method to provide update on progress and for delivery of the
 * product. 
 * The observer pattern is used such that the factory can
 * notify the ordering class about progress being made or
 * about product delivery.
 * @author Peter Kemper
 *
 */
public interface Factory {
	/**
	 * Takes or rejects an order. The method returns immediately
	 * and signals if the order is accepted or not. The actual
	 * production is performed in a background thread and 
	 * delivery of the ordered MazeConfiguration takes place
	 * by calling the deliver method of the given order object.
	 * @param order specifies what kind of maze is wanted and the result once delivered
	 * @return true if order is accepted, false if refused
	 */
	public boolean order(Order order) ;
	/**
	 * Cancels the current order if there is any that is not 
	 * not completed yet.
	 */
	public void cancel() ;
	/**
	 * Wait till order is fulfilled. This method returns after
	 * the produced maze has been delivered. 
	 * This method can be used to have a more synchronous behavior
	 * which is helpful for testing for example.
	 */
	public void waitTillDelivered() ;
}
