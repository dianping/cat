namespace Org.Unidal.Cat.Message
{
    ///<summary>
    ///  <p>
    ///    <c>Event</c>
    ///    is used to log anything interesting happens at a specific
    ///    time. Such as an exception thrown, a review added by user, a new user
    ///    registered, an user logged into the system etc.</p> <p>However, if it could be failure, or last for a long time, such as a remote
    ///                                                          API call, database call or search engine call etc. It should be logged as a
    ///                                                          <c>Transaction</c>
    ///                                                        </p> <p>All CAT message will be constructed as a message tree and send to back-end
    ///                                                               for further analysis, and for monitoring. Only
    ///                                                               <c>Transaction</c>
    ///                                                               can
    ///                                                               be a tree node, all other message will be the tree leaf.?The transaction
    ///                                                               without other messages nested is an atomic transaction.</p>
    ///</summary>
    public interface IEvent : IMessage
    {
    }
}