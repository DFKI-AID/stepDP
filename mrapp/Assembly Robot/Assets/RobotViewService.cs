using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DFKI.AssemblyRobot;
using Thrift.Transport;
using Thrift.Server;
using System.Threading.Tasks;

public class RobotViewService : MonoBehaviour, AssemblyRobotView.Iface
{
    public int Port = 11000;
    private TServer server;
    public void updateGrammar(UpdateGrammar updateGrammar)
    {
        Debug.Log($"Received {updateGrammar}");
        
        //TODO impl;
    }

    // Start is called before the first frame update
    void Start()
    {
        Task t = new Task(() =>
        {
            Debug.Log($"Binding thrift service to {Port}");
            var proc = new AssemblyRobotView.Processor(this);
            var trans = new TServerSocket(Port);
            server = new TThreadPoolServer(proc, trans);
            server.Serve();
        });
        t.Start();
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnDestroy()
    {
        if (server != null)
        {
            server.Stop();
        }
    }
}
