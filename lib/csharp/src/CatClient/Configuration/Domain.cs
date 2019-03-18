namespace Org.Unidal.Cat.Configuration
{
    /// <summary>
    ///   描述当前系统的情况
    /// </summary>
    public class Domain
    {
        private string _id = CatConstants.UNKNOWN_DOMAIN;
        private bool _mEnabled = true;
        private int _mMaxMessageSize = 1000;

        /// <summary>
        ///   当前系统的标识
        /// </summary>
        public string Id
        {
            get { return _id; }
            set { _id = value; }
        }

        ///// <summary>
        /////   当前系统的IP
        ///// </summary>
        //public string Ip { get; set; }

        /// <summary>
        ///   Cat日志是否开启，默认开启
        /// </summary>
        public bool Enabled
        {
            get { return _mEnabled; }
            set { _mEnabled = value; }
        }

        /// <summary>
        ///   当内存中的消息数量大于这个值时，会截短消息树，将截短的部分发送到服务端。
        /// </summary>
        public int MaxMessageSize
        {
            get { return _mMaxMessageSize;  }
            set { _mMaxMessageSize = value; }
        }
    }
}