using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;

public static class NetPortFinder
{
    /*
        返回指定进程正在监听的网络端口（未找到则返回空串）
    */
    public static string GetPortByProcessName(string processName)
    {
        if (string.IsNullOrWhiteSpace(processName))
            return "";

        // 兼容用户输入 "xxx.exe" 的情况
        if (processName.EndsWith(".exe", StringComparison.OrdinalIgnoreCase))
        {
            processName = processName.Substring(0, processName.Length - 4);
        }

        string port = "";

        // 获取目标进程的 PID 列表
        var processes = Process.GetProcessesByName(processName);
        HashSet<int> targetPids = new HashSet<int>();
        foreach (var proc in processes)
        {
            targetPids.Add(proc.Id);
        }

        // 获取所有 TCP 监听连接
        foreach (var row in GetAllTcpConnections())
        {
            if (row.State == MibTcpState.Listen && targetPids.Contains(row.OwningPid))
            {
                port = row.LocalPort.ToString();
                break;
            }
        }

        return port;
    }

    private static IEnumerable<TcpRow> GetAllTcpConnections()
    {
        int bufferSize = 0;
        GetExtendedTcpTable(IntPtr.Zero, ref bufferSize, true, AF_INET, TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);

        IntPtr tcpTablePtr = Marshal.AllocHGlobal(bufferSize);
        try
        {
            int result = GetExtendedTcpTable(tcpTablePtr, ref bufferSize, true, AF_INET, TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);
            if (result != 0) yield break;

            int rowCount = Marshal.ReadInt32(tcpTablePtr);
            IntPtr rowPtr = IntPtr.Add(tcpTablePtr, 4); // 跳过 row count

            for (int i = 0; i < rowCount; i++)
            {
                var row = Marshal.PtrToStructure<MIB_TCPROW_OWNER_PID>(rowPtr);

                yield return new TcpRow
                {
                    State = row.state,
                    LocalPort = BitConverter.ToUInt16(new byte[2] { row.localPort[1], row.localPort[0] }, 0),
                    OwningPid = (int)row.owningPid
                };

                rowPtr = IntPtr.Add(rowPtr, Marshal.SizeOf<MIB_TCPROW_OWNER_PID>());
            }
        }
        finally
        {
            Marshal.FreeHGlobal(tcpTablePtr);
        }
    }

    // 内部结构封装
    private class TcpRow
    {
        public MibTcpState State;
        public int LocalPort;
        public int OwningPid;
    }

    private const int AF_INET = 2;

    private enum TCP_TABLE_CLASS
    {
        TCP_TABLE_BASIC_LISTENER,
        TCP_TABLE_BASIC_CONNECTIONS,
        TCP_TABLE_BASIC_ALL,
        TCP_TABLE_OWNER_PID_LISTENER,
        TCP_TABLE_OWNER_PID_CONNECTIONS,
        TCP_TABLE_OWNER_PID_ALL,
        TCP_TABLE_OWNER_MODULE_LISTENER,
        TCP_TABLE_OWNER_MODULE_CONNECTIONS,
        TCP_TABLE_OWNER_MODULE_ALL
    }

    private enum MibTcpState
    {
        Closed = 1,
        Listen = 2,
        SynSent = 3,
        SynReceived = 4,
        Established = 5,
        FinWait1 = 6,
        FinWait2 = 7,
        CloseWait = 8,
        Closing = 9,
        LastAck = 10,
        TimeWait = 11,
        DeleteTcb = 12
    }

    [StructLayout(LayoutKind.Sequential)]
    private struct MIB_TCPROW_OWNER_PID
    {
        public MibTcpState state;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] localAddr;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] localPort;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] remoteAddr;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] remotePort;

        public uint owningPid;
    }

    [DllImport("iphlpapi.dll", SetLastError = true)]
    private static extern int GetExtendedTcpTable(
        IntPtr pTcpTable,
        ref int dwOutBufLen,
        bool sort,
        int ipVersion,
        TCP_TABLE_CLASS tblClass,
        int reserved
    );
}