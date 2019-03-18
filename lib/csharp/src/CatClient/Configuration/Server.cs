namespace Org.Unidal.Cat.Configuration
{
    /// <summary>
    ///   描述记录当前系统日志的目标Cat服务器
    /// </summary>
    public class Server
    {
        private readonly string _mIp;

        private readonly int _mPort;

        private int httpPort;

        public Server(string ip, int port = 2280, int hp = 8080)
        {
            _mIp = ip;
            _mPort = port;
            httpPort = hp;
            Enabled = true;
        }

        /// <summary>
        ///   Cat服务器IP
        /// </summary>
        public string Ip
        {
            get { return _mIp; }
        }

        /// <summary>
        ///   Cat服务器端口
        /// </summary>
        public int Port
        {
            get { return _mPort; }
        }

        /// <summary>
        ///   远程获取Cat客户端配置信息的Http端口
        /// </summary>
        public int HttpPort
        {
            get { return httpPort; }
        }

        /// <summary>
        ///   Cat服务器是否有效，默认有效
        /// </summary>
        public bool Enabled { get; set; }

        public override string ToString()
        {
            return Ip + ":" + Port + " (http:" + HttpPort + ")";
        }
    }
}