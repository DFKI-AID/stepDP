using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;
using UnityEngine.Windows.Speech;

public class ASRScript : MonoBehaviour
{
    private GrammarRecognizer grammarRecognizer;

    // Start is called before the first frame update
    void Start()
    {
        UpdateGrammar(null);
        SaveGrammar("foo");
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    // SRGS grammar (xml format)
    public void UpdateGrammar(string srgsGrammar)
    {
        Debug.Log($"Updating grammar");
        if(grammarRecognizer != null)
        {
            grammarRecognizer.Stop();
        }
        var path = Application.dataPath + "/asr/StreamingAssets" + "/example.xml";
        this.grammarRecognizer = new GrammarRecognizer(path);
        grammarRecognizer.OnPhraseRecognized += OnPhraseRecognized;
        grammarRecognizer.Start();
    }

    public void SaveGrammar(string grammar)
    {
        var path = Application.persistentDataPath + "/example2.xml";
        Debug.Log($"Saving grammar to {path}");
        using (StreamWriter streamWriter = File.CreateText(path))
        {
            streamWriter.Write(grammar);
        }
    }

    private void OnPhraseRecognized(PhraseRecognizedEventArgs args)
    {
        SemanticMeaning[] meanings = args.semanticMeanings;
        Debug.Log("srgs: " + meanings);
    }
}
