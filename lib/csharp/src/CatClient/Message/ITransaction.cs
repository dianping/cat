using System.Collections.Generic;

namespace Org.Unidal.Cat.Message
{
    /**
	 * <p>
	 * <code>Transaction</code> is any interesting unit of work that takes time to
	 * complete and may fail.
	 * </p>
	 * 
	 * <p>
	 * Basically, all data access across the boundary needs to be logged as a
	 * <code>Transaction</code> since it may fail and time consuming. For example,
	 * URL request, disk IO, JDBC query, search query, HTTP request, 3rd party API
	 * call etc.
	 * </p>
	 * 
	 * <p>
	 * Sometime if A needs call B which is owned by another team, although A and B
	 * are deployed together without any physical boundary. To make the ownership
	 * clear, there could be some <code>Transaction</code> logged when A calls B.
	 * </p>
	 * 
	 * <p>
	 * Most of <code>Transaction</code> should be logged in the infrastructure level
	 * or framework level, which is transparent to the application.
	 * </p>
	 * 
	 * <p>
	 * All CAT message will be constructed as a message tree and send to back-end
	 * for further analysis, and for monitoring. Only <code>Transaction</code> can
	 * be a tree node, all other message will be the tree leaf.　The transaction
	 * without other messages nested is an atomic transaction.
	 * </p>
	 * 
	 * @author Frankie Wu
	 */

    public interface ITransaction : IMessage
    {
        /**
		 * Get all children message within current transaction.
		 * 
		 * <p>
		 * Typically, a <code>Transaction</code> can nest other
		 * <code>Transaction</code>s, <code>Event</code>s and <code>Heartbeat</code>
		 * s, while an <code>Event</code> or <code>Heartbeat</code> can't nest other
		 * messages.
		 * </p>
		 * 
		 * @return all children messages, empty if there is no nested children.
		 */
        IList<IMessage> Children { get; }

        /**
		 * How long the transaction took from construction to complete. Time unit is
		 * microsecond.
		 * 
		 * @return duration time in microsecond
		 */
        long DurationInMicros { get; set; }

        /**
		 * How long the transaction took from construction to complete. Time unit is
		 * millisecond.
		 * 
		 * @return duration time in millisecond
		 */
        long DurationInMillis { get; set; }

        /**
		 * Check if the transaction is stand-alone or belongs to another one.
		 * 
		 * @return true if it's an root transaction.
		 */
        bool Standalone { get; set; }

        /**
		 * Add one nested child message to current transaction.
		 * 
		 * @param message
		 *           to be added
		 */
        ITransaction AddChild(IMessage message);

        /**
		 * Has children or not. An atomic transaction does not have any children
		 * message.
		 * 
		 * @return true if child exists, else false.
		 */
        bool HasChildren();
    }
}