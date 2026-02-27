using System;
using System.Net.Sockets;
using System.Net;
using System.Web;

namespace Org.Unidal.Cat.Util
{
    public class NetworkInterfaceManager
    {
        private static string _hostName = string.Empty;
        private static string _hostIp = string.Empty;
        private static byte[] _hostAddressBytes;

        static NetworkInterfaceManager()
        {
            Refresh();
        }

        public static void Refresh()
        {
            _hostName = System.Net.Dns.GetHostName();
            IPHostEntry ipHostEntry = Dns.GetHostEntry(_hostName);
            _hostIp = GetIP(ipHostEntry);
            _hostAddressBytes = GetAddressBytes(ipHostEntry);
        }

        private static string GetIP(IPHostEntry ipHostEntry)
        {
            foreach (IPAddress ip in ipHostEntry.AddressList)
            {
                if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                {
                    return ip.ToString();
                }
            }
            return ipHostEntry.AddressList[0].ToString();
        }

        private static byte[] GetAddressBytes(IPHostEntry ipHostEntry)
        {
            foreach (IPAddress ip in ipHostEntry.AddressList)
            {
                if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                {
                    return ip.GetAddressBytes();
                }
            }
            return ipHostEntry.AddressList[0].GetAddressBytes();
        }


        public static string HostName
        {
            get
            {
                return _hostName ?? "";
            }
        }

        public static string HostIP
        {
            get
            {
                return _hostIp ?? "127.0.0.1";
            }
        }

        public static byte[] AddressBytes
        {
            get
            {
                return _hostAddressBytes;
            }
        }

        /// <summary>
        /// //获取客户端的地址,获取顺序1.HTTP_X_FORWARDED_FOR;2.REMOTE_ADDR;
        /// </summary>
        /// <returns>客户端IP</returns>
        public static string GetClientIP()
        {
            // 穿过代理服务器取远程用户真实IP地址
            string ip = string.Empty;

            #if NETFULL
            try
            {
                ip = HttpContext.Current.Request.Headers["X-Forwarded-For"];
                if (string.IsNullOrEmpty(ip))
                {
                    ip = HttpContext.Current.Request.ServerVariables["REMOTE_ADDR"];
                }
           
                if (!string.IsNullOrEmpty(ip) && ip.Contains(","))
                {
                    ip = ip.Split(',')[0];
                }

            }
            catch (Exception e){ ip = ""; Cat.lastException = e;}
            #endif

            return ip;
        }
    }
}