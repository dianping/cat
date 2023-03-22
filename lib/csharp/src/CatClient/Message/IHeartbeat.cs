namespace Org.Unidal.Cat.Message
{
    ///<summary>
    ///  <p>
    ///    <c>Heartbeat</c>
    ///    is used to log data that happens in a regular
    ///    intervals, for example once per second, such as system load, CPU percentage,
    ///    memory usage, thread pool statistics, cache hit/miss rate, service manifest
    ///    etc., and even some configuration could be carried by
    ///    <c>Heartbeat</c>
    ///    .
    ///    There could be some good use cases, for example health checker and load
    ///    balancer, that make good use of it.</p> <p>
    ///                                              <c>Heartbeat</c>
    ///                                              should never be used per request since the request is
    ///                                              not regular predictable, instead it could be logged in a daemon background
    ///                                              thread, or something like a Timer.</p> <p>All CAT message will be constructed as a message tree and send to back-end
    ///                                                                                       for further analysis, and for monitoring. Only
    ///                                                                                       <c>Transaction</c>
    ///                                                                                       can
    ///                                                                                       be a tree node, all other message will be the tree leaf.?The transaction
    ///                                                                                       without other messages nested is an atomic transaction.</p>
    ///</summary>
    public interface IHeartbeat : IMessage
    {
    }
}