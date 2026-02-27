using System;
using System.Collections.Generic;

namespace Org.Unidal.Cat.Message.Spi
{
    ///<summary>
    ///  <p>Message factory is used to create new transaction,event and/or heartbeat.</p> <p>Normally, application code logs message in following ways, for example:
    ///                                                                                     <ul>
    ///                                                                                       <li>Event
    ///                                                                                         <pre>public class MyClass { 
    ///                                                                                           public static MessageFactory CAT = Cat.getFactory();
    ///                                                                                           public void bizMethod() { 
    ///                                                                                           Event event = CAT.newEvent("Review", "New");
    ///                                                                                           event.addData("id", 12345); 
    ///                                                                                           event.addData("user", "john");
    ///                                                                                           ...
    ///                                                                                           event.setStatus("0"); 
    ///                                                                                           event.complete(); 
    ///                                                                                           }
    ///                                                                                           ...
    ///                                                                                           }</pre>
    ///                                                                                       </li>
    ///                                                                                       <li>Heartbeat
    ///                                                                                         <pre>public class MyClass { 
    ///                                                                                           public static MessageFactory CAT = Cat.getFactory();
    ///                                                                                           public void bizMethod() { 
    ///                                                                                           Heartbeat event = CAT.newHeartbeat("System", "Status");
    ///                                                                                           event.addData("ip", "192.168.10.111");
    ///                                                                                           event.addData("host", "host-1");
    ///                                                                                           event.addData("load", "2.1");
    ///                                                                                           event.addData("cpu", "0.12,0.10");
    ///                                                                                           event.addData("memory.total", "2G");
    ///                                                                                           event.addData("memory.free", "456M");
    ///                                                                                           event.setStatus("0");
    ///                                                                                           event.complete();
    ///                                                                                           }
    ///                                                                                           ...
    ///                                                                                           }</pre>
    ///                                                                                       </li>
    ///                                                                                       <li>Transaction
    ///                                                                                         <pre>public class MyClass { 
    ///                                                                                           public static MessageFactory CAT = Cat.getFactory();
    ///                                                                                           public void bizMethod() { 
    ///                                                                                           Transaction t = CAT.newTransaction("URL", "MyPage");
    ///                                                                                           try {
    ///                                                                                           // do your business here
    ///                                                                                           t.addData("k1", "v1");
    ///                                                                                           t.addData("k2", "v2");
    ///                                                                                           t.addData("k3", "v3");
    ///                                                                                           Thread.sleep(30);
    ///                                                                                           t.setStatus("0");
    ///                                                                                           } catch (Exception e) {
    ///                                                                                           t.setStatus(e);
    ///                                                                                           } finally {
    ///                                                                                           t.complete();
    ///                                                                                           }
    ///                                                                                           }
    ///                                                                                           ...
    ///                                                                                           }</pre>
    ///                                                                                       </li>
    ///                                                                                     </ul>
    ///                                                                                     or logs event or heartbeat in one shot, for example:
    ///                                                                                     <ul>
    ///                                                                                       <li>Event
    ///                                                                                         <pre>public class MyClass { 
    ///                                                                                           public static MessageFactory CAT = Cat.getFactory();
    ///                                                                                           public void bizMethod() { 
    ///                                                                                           CAT.logEvent("Review", "New", "0", "id=12345&user=john");
    ///                                                                                           }
    ///                                                                                           ...
    ///                                                                                           }</pre>
    ///                                                                                       </li>
    ///                                                                                       <li>Heartbeat
    ///                                                                                         <pre>public class MyClass { 
    ///                                                                                           public static MessageFactory CAT = Cat.getFactory();
    ///                                                                                           public void bizMethod() { 
    ///                                                                                           CAT.logHeartbeat("System", "Status", "0", "ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M");
    ///                                                                                           }
    ///                                                                                           ...
    ///                                                                                           }</pre>
    ///                                                                                       </li>
    ///                                                                                     </ul>
    ///                                                                                   </p>
    ///</summary>
    public interface IMessageProducer
    {
        ///<summary>
        ///  Log an error.
        ///</summary>
        ///<param name="cause"> root cause exception </param>
        void LogError(Exception cause);

        ///<summary>
        ///  Log an error.
        ///</summary>
        ///<param name="cause"> root cause exception </param>
        void LogError(String message, Exception cause);

        ///<summary>
        ///  Log an event in one shot.
        ///</summary>
        ///<param name="type"> event type </param>
        ///<param name="name"> event name </param>
        ///<param name="status"> "0" means success, otherwise means error code </param>
        ///<param name="nameValuePairs"> name value pairs in the format of "a=1&b=2&..." </param>
        void LogEvent(String type, String name, String status, String nameValuePairs);

        ///<summary>
        ///  Log a heartbeat in one shot.
        ///</summary>
        ///<param name="type"> heartbeat type </param>
        ///<param name="name"> heartbeat name </param>
        ///<param name="status"> "0" means success, otherwise means error code </param>
        ///<param name="nameValuePairs"> name value pairs in the format of "a=1&b=2&..." </param>
        void LogHeartbeat(String type, String name, String status, String nameValuePairs);

        void LogMetric(String name, String status, String nameValuePairs);

        IMetric NewMetric(String type, String name, String data);

        ///<summary>
        ///  Create a new event with given type and name.
        ///</summary>
        ///<param name="type"> event type </param>
        ///<param name="name"> event name </param>
        IEvent NewEvent(String type, String name, String data);

        ///<summary>
        ///  Create a new heartbeat with given type and name.
        ///</summary>
        ///<param name="type"> heartbeat type </param>
        ///<param name="name"> heartbeat name </param>
        IHeartbeat NewHeartbeat(String type, String name, String data);

        ///<summary>
        ///  Create a new transaction with given type and name.
        ///</summary>
        ///<param name="type"> transaction type </param>
        ///<param name="name"> transaction name </param>
        ITransaction NewTransaction(String type, String name);

        ///<summary>
        ///  Create a forked transaction for a child thread.
        ///</summary>
        ///<param name="type"> transaction type </param>
        ///<param name="name"> transaction name </param>
        IForkedTransaction NewForkedTransaction(String type, String name);

        ///<summary>
        ///  Create a tagged transaction for another process or thread.
        ///</summary>
        ///<param name="type"> transaction type </param>
        ///<param name="name"> transaction name </param>
        ///<param name="tag"> tag applied to the transaction </param>
        ITaggedTransaction NewTaggedTransaction(String type, String name, String tag);
    }
}