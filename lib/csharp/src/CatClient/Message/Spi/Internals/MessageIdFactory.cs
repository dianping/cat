using Org.Unidal.Cat.Util;
using System;
using System.Text;
using System.IO;
using System.IO.MemoryMappedFiles;
using System.Security.Permissions;
using System.Security;
using System.Linq;
using System.Threading;
using System.Diagnostics;
using System.Collections.Generic;
using System.Collections.Concurrent;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    /// <summary>
    ///   根据域名（配置指定的），系统IP（自动解析的，16进制字符串），时间戳（1970年到当前的小时数）和自增编号组成
    /// </summary>
    public class MessageIdFactory : IDisposable
    {
        private const int MAX_REUSED_IDS_SIZE = 100000;

        private long _mTimestamp;

        // timestamp.ToString() is expensive. So we cache the timestamp string for current hour.
        private string _mTimestampStr = null;

        private int _mIndex;

        private String _mDomain;

        private String _mIpAddress;

        private MemoryMappedViewAccessor memoryMappedViewAccessor;

        private BlockingThreadSafeQueue<string> _mReusedIds = new BlockingThreadSafeQueue<string>();

        public static DateTime _mLastMarkFlush = DateTime.MinValue;

        public MessageIdFactory()
        {
            var initialTimestamp = Timestamp;
            _mTimestamp = initialTimestamp;
            _mTimestampStr = initialTimestamp.ToString();
        }

        ~MessageIdFactory()
        {
            Dispose();
        }

        public void Dispose()
        {
            try
            {
                if (null != memoryMappedViewAccessor)
                {
                    memoryMappedViewAccessor.Flush();
                    _mLastMarkFlush = DateTime.Now;
                    memoryMappedViewAccessor.Dispose();
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        protected internal long Timestamp
        {
            get { return MilliSecondTimer.CurrentTimeHoursForJava(); }
        }

        public String Domain
        {
            set { _mDomain = value; }
        }

        public String IpAddress
        {
            set { _mIpAddress = value; }
        }

        public String GetNextId()
        {
            string id;
            _mReusedIds.TryDequeue(out id, false);
            if (null != id)
            {
                return id;
            }

            long timestamp = Timestamp;

            if (timestamp != _mTimestamp)
            {
                _mIndex = 0;
                _mTimestamp = timestamp;
                _mTimestampStr = timestamp.ToString();
            }

            int index = Interlocked.Increment(ref _mIndex);

            //StringBuilder sb = new StringBuilder(_mDomain.Length + 32);

            //sb.Append(_mDomain);
            //sb.Append('-');
            //sb.Append(_mIpAddress);
            //sb.Append('-');
            //sb.Append(timestamp);
            //sb.Append('-');
            //sb.Append(index);

            //return sb.ToString();

            id = String.Join("-", new string[] { _mDomain, _mIpAddress, _mTimestampStr, index.ToString() });

            SaveMark(false);

            return id;
        }

        public void Reuse(string id)
        {
            if (_mReusedIds.Count < MAX_REUSED_IDS_SIZE)
            {
                _mReusedIds.Enqueue(id);
            }
        }

        public void Initialize(String domain)
        {
            _mDomain = domain;

            if (_mIpAddress == null)
            {
                byte[] bytes = NetworkInterfaceManager.AddressBytes;

                StringBuilder sb = new StringBuilder();

                foreach (byte b in bytes)
                {
                    sb.Append(((b >> 4) & 0x0F).ToString("x"));
                    sb.Append((b & 0x0F).ToString("x"));
                }

                _mIpAddress = sb.ToString();
            }

            // !Important, force GC for MessageIdFactory of the previous process, so that memory mapped file is disposed.
            GC.Collect();
            memoryMappedViewAccessor = CreateOrOpenMarkFile(_mDomain);
            if (null != memoryMappedViewAccessor && memoryMappedViewAccessor.CanRead)
            {
                var index = memoryMappedViewAccessor.ReadInt32(CatConstants.ID_MARK_FILE_INDEX_OFFSET);
                var lastTimestamp = memoryMappedViewAccessor.ReadInt64(CatConstants.ID_MARK_FILE_TS_OFFSET);
                if (lastTimestamp == _mTimestamp)
                {
                    _mIndex = index + 10000;
                }
                else
                {
                    _mIndex = 0;
                }
            }

            SaveMark(true);
        }

        public void SaveMark(bool flush = false)
        {
            try
            {
                if (null != memoryMappedViewAccessor && memoryMappedViewAccessor.CanWrite)
                {
                    memoryMappedViewAccessor.Write(CatConstants.ID_MARK_FILE_INDEX_OFFSET, _mIndex);
                    memoryMappedViewAccessor.Write(CatConstants.ID_MARK_FILE_TS_OFFSET, _mTimestamp);
                    if (flush) {
                        memoryMappedViewAccessor.Flush();
                        _mLastMarkFlush = DateTime.Now;
                    }
                };
            }
            catch (Exception ex)
            { 
                Cat.lastException = ex; 
            }
        }

        private MemoryMappedFile CreateFileIfMissing(string dir, string fileName)
        {
            string filePath = Path.Combine(dir, fileName);
            if (!File.Exists(filePath))
            {
                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }
            }
            
            var mmfName = CatConstants.ID_MARK_FILE_MAP + "-" + fileName;
            MemoryMappedFile mmf = null;
            try
            {
                mmf = MemoryMappedFile.OpenExisting(mmfName, MemoryMappedFileRights.ReadWrite, HandleInheritability.Inheritable);
            }
            catch (System.IO.FileNotFoundException)
            {
                var stream = File.Open(filePath, FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.ReadWrite);
#if NETFULL
                MemoryMappedFileSecurity security = new MemoryMappedFileSecurity();
                mmf = MemoryMappedFile.CreateFromFile(stream, mmfName, CatConstants.ID_MARK_FILE_SIZE,
                    MemoryMappedFileAccess.ReadWrite, security, HandleInheritability.Inheritable, false);
#else
                mmf = MemoryMappedFile.CreateFromFile(stream, mmfName, CatConstants.ID_MARK_FILE_SIZE,
                   MemoryMappedFileAccess.ReadWrite, HandleInheritability.Inheritable, false);
#endif
            }
            return mmf;
        }

        private MemoryMappedViewAccessor CreateOrOpenMarkFile(string domain)
        {
            string fileName = "cat-" + domain + ".mark";
            MemoryMappedFile mmf = null;

            string[] dirs = new string[] {CatConstants.CAT_FILE_DIR, Path.GetTempPath(),
                AppDomain.CurrentDomain.BaseDirectory + @"\data\appdatas\cat"};

            bool isFirst = true;
            foreach (string dir in dirs)
            {
                if (null == mmf)
                {
                    try
                    {
                        mmf = CreateFileIfMissing(dir, fileName);
                        break;
                    }
                    catch (Exception ex)
                    {
                        if (isFirst)
                        {
                            Cat.lastException = ex;
                        }
                    }
                }
                isFirst = false;
            }

            if (null == mmf)
            {
                // Trial 4: If failed to create a temp persistent MMF, create a non-persistent MMF.
                try
                {
                    var mmfName = CatConstants.ID_MARK_FILE_MAP + "-" + domain;
                    mmf = MemoryMappedFile.CreateOrOpen(mmfName, CatConstants.ID_MARK_FILE_SIZE, MemoryMappedFileAccess.ReadWrite);
                }
                catch (Exception ex)
                { Cat.lastException = ex; }
            }

            if (mmf != null)
            {
                MemoryMappedViewAccessor accessor = mmf.CreateViewAccessor();
                return accessor;
            }
            return null;
        }
    }
}