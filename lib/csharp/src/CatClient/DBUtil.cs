using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Org.Unidal.Cat
{
    public class DBUtil
    {
        private static AsyncLocal<Message.ITransaction> _scopedTrans = new AsyncLocal<Message.ITransaction>();
        private static readonly ConcurrentDictionary<string, string> filePathDic = new ConcurrentDictionary<string, string>();

        public static TResult WrapWithCatTransaction<TResult>(Func<TResult> sqlFunc, string queryCatetroy, string operationType,
            [CallerFilePath] string callerFilePath = "", [CallerMemberName] string callerMemberName = "", [CallerLineNumber] int callerLineNumber = 0)
        {
            if (!Org.Unidal.Cat.Cat.Enabled)
            {
                return sqlFunc();
            }

            bool ownTrans = true;
            var catTran = StartSqlTransaction(queryCatetroy, ref ownTrans);
            try
            {
                Cat.LogEvent(CatConstants.EVENT_SQL_METHOD, operationType, CatConstants.SUCCESS, GetCallerInfo(callerFilePath, callerMemberName, callerLineNumber));

                var result = sqlFunc();

                var listResult = result as System.Collections.ICollection;
                if (listResult != null)
                {
                    LogEventForRowCount(listResult.Count);
                }

                return result;
            }
            catch (Exception e)
            {
                catTran.SetStatus(e);
                throw;
            }
            finally
            {
                if (ownTrans)
                {
                    catTran.Status = CatConstants.SUCCESS;
                    catTran.Complete();
                    _scopedTrans.Value = null;
                }
            }
        }

        public static async Task<TResult> WrapWithCatTransactionAsync<TResult>(Func<Task<TResult>> sqlFunc, string queryCatetroy, string operationType,
            [CallerFilePath] string callerFilePath = "", [CallerMemberName] string callerMemberName = "", [CallerLineNumber] int callerLineNumber = 0)
        {
            if (!Org.Unidal.Cat.Cat.Enabled)
            {
                return await sqlFunc();
            }

            bool ownTrans = true;
            var catTran = StartSqlTransaction(queryCatetroy, ref ownTrans);
            try
            {
                Cat.LogEvent(CatConstants.EVENT_SQL_METHOD, operationType, CatConstants.SUCCESS, GetCallerInfo(callerFilePath, callerMemberName, callerLineNumber));

                var result = await sqlFunc();

                var listResult = result as System.Collections.ICollection;
                if (listResult != null)
                {
                    LogEventForRowCount(listResult.Count);
                }

                return result;
            }
            catch (Exception e)
            {
                catTran.SetStatus(e);
                throw;
            }
            finally
            {
                if (ownTrans)
                {
                    catTran.Status = CatConstants.SUCCESS;
                    catTran.Complete();
                    _scopedTrans.Value = null;
                }
            }
        }

        public static void WrapWithCatTransaction(Action sqlAction, string queryCatetroy, string operationType,
             [CallerFilePath] string callerFilePath = "", [CallerMemberName] string callerMemberName = "", [CallerLineNumber] int callerLineNumber = 0)
        {
            if (!Org.Unidal.Cat.Cat.Enabled)
            {
                sqlAction();
                return;
            }

            bool ownTrans = true;
            var catTran = StartSqlTransaction(queryCatetroy, ref ownTrans);
            try
            {
                Cat.LogEvent(CatConstants.EVENT_SQL_METHOD, operationType, CatConstants.SUCCESS, GetCallerInfo(callerFilePath, callerMemberName, callerLineNumber));

                sqlAction();
            }
            catch (Exception e)
            {
                catTran.SetStatus(e);
                throw;
            }
            finally
            {
                if (ownTrans)
                {
                    catTran.Status = CatConstants.SUCCESS;
                    catTran.Complete();
                    _scopedTrans.Value = null;
                }
            }
        }

        public static async Task WrapWithCatTransactionAsync(Func<Task> sqlAction, string queryCatetroy, string operationType,
        [CallerFilePath] string callerFilePath = "", [CallerMemberName] string callerMemberName = "", [CallerLineNumber] int callerLineNumber = 0)
        {
            if (!Org.Unidal.Cat.Cat.Enabled)
            {
                await sqlAction();
                return;
            }

            bool ownTrans = true;
            var catTran = StartSqlTransaction(queryCatetroy, ref ownTrans);
            try
            {
                Cat.LogEvent(CatConstants.EVENT_SQL_METHOD, operationType, CatConstants.SUCCESS, GetCallerInfo(callerFilePath, callerMemberName, callerLineNumber));

                await sqlAction();

            }
            catch (Exception e)
            {
                catTran.SetStatus(e);
                throw;
            }
            finally
            {
                if (ownTrans)
                {
                    catTran.Status = CatConstants.SUCCESS;
                    catTran.Complete();
                    _scopedTrans.Value = null;
                }
            }
        }

        public static void LogEventForRowCount(int rowCount)
        {
            if (!Org.Unidal.Cat.Cat.Enabled)
            {
                return;
            }

            var currTrans = Cat.GetManager()?.PeekTransaction();
            if (currTrans == null)
            {
                currTrans = _scopedTrans.Value;
                if (currTrans == null)
                    return;
            }

            var countMessage = string.Empty;
            if (rowCount < 10)
                countMessage = "<10";
            else if (rowCount < 100)
                countMessage = "<100";
            else if (rowCount < 1000)
                countMessage = "<1000";
            else if (rowCount < 5000)
                countMessage = "<5000";
            else if (rowCount < 10000)
                countMessage = "<10000";
            else
                countMessage = ">10000";

            Cat.LogEvent(CatConstants.EVENT_SQL_ROWS, countMessage, CatConstants.SUCCESS, rowCount.ToString());
        }

        private static Message.IMessage StartSqlTransaction(string queryCatetroy, ref bool ownTrans)
        {
            var currTrans = _scopedTrans.Value;
            if (currTrans != null)
            {
                ownTrans = false;
                return currTrans;
            }

            currTrans = Cat.NewTransaction(CatConstants.EVENT_SQL, queryCatetroy);
            _scopedTrans.Value = currTrans;

            return currTrans;
        }

        private static string GetCallerInfo(string callerFilePath, string callerMemberName, int callerLineNumber)
        {
            var path = filePathDic.GetOrAdd(callerFilePath, (filePath) =>
            {
                var index = filePath.LastIndexOf('\\');
                if (index > 0)
                    return filePath.Substring(index + 1);
                else
                    return filePath;
            });

            return $"Caller={path}&Member={callerMemberName}&Line={callerLineNumber.ToString()}";
        }
    }
}
