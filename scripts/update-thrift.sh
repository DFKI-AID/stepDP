#!/bin/bash
thrift-0.12 --gen java --out ../rengine/src/main/java/ ../mrapp.thrift
thrift-0.12 --gen csharp --out ../mrapp/ ../mrapp.thrift
