using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Org.Unidal.Cat;
using System.Threading;
using System.Reflection;

namespace CatClientTest
{
    class NestedExceptionTest
    {
        public static void Test()
        {
            var transaction = Cat.NewTransaction("Nested exception test 2", "transaction");
            try
            {
                Student stu = new Student();
                //Foo(stu);
                MethodInfo mi = typeof(NestedExceptionTest).GetMethod("Foo", new Type[]{typeof(Person)});
                mi.Invoke(null, new object[] { stu });
                transaction.Status = "0";
            }
            catch (Exception ex)
            {
                var wrapperException = new Exception("Exception caught in Test()", ex);
                Console.WriteLine("Exception happens. [{0}]", wrapperException);
                Cat.LogError("Some message happens.", wrapperException);
                transaction.SetStatus(wrapperException);
            }
            finally
            {
                transaction.Type = "NestedExceptionTest";
                transaction.Complete();
            }
        }

        public static void Foo(Person person)
        {
            try
            {
                Console.WriteLine("This is foo.");
                Bar(person);
            }
            catch (ApplicationException ae)
            {
                throw new CustomException("Custom exception thrown by Foo()", ae);
            }
        }

        private static void Bar(Person person)
        {
            try
            {
                // person.ToString() triggers null reference exception.
                Console.WriteLine("Person: [{0}]", person.ToString());
            }
            catch (Exception ex)
            {
                throw new ApplicationException("Application exception thrown by Bar()", ex);
            }
        }
    }

    class CustomException : Exception
    {
        public CustomException(string msg, ApplicationException ex) : base(msg, ex)
        {}
    }

    abstract class Person
    {
        public override string ToString()
        {
            return "[" + GetName().ToString() + "]";
        }

        public abstract string GetName();
    }

    class Student : Person
    {
        public override string GetName()
        {
            return null;
        }
    }
}
