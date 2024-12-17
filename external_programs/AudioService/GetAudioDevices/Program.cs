using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

/*
    获取所有音频设备的设备 ID 和设备名称。
    输出格式：
    "
        设备ID1 设备名称1
        设备ID2 设备名称2
        设备ID3 设备名称3
    "
*/
class Program
{
    static void Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;

        var enumerator = new MMDeviceEnumerator();
        var devices = enumerator.EnumAudioEndpoints(DataFlow.Render, DeviceState.Active);
        foreach (var device in devices)
        {
            Console.WriteLine(device.DeviceID + " " + device.FriendlyName);
        }
    }
}