#!/bin/bash
ping -c1 -w 2 int1-h1 1>/dev/null -q || echo -ne "\n====\nERROR H1 de la subred INTNet1 \n==== \n";
ping -c1 -w 2 int1-h2 1>/dev/null -q || echo -ne "\n====\nERROR H2 de la subred INTNet1 \n==== \n";
ping -c1 -w 2 int1-h3 1>/dev/null -q || echo -ne "\n====\nERROR H3 de la subred INTNet1 \n==== \n";
ping -c1 -w 2 int1-h4 1>/dev/null -q || echo -ne "\n====\nERROR H4 de la subred INTNet1 \n==== \n";
ping -c1 -w 2 int2-h1 1>/dev/null -q || echo -ne "\n====\nERROR H1 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int2-h2 1>/dev/null -q || echo -ne "\n====\nERROR H2 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int2-h3 1>/dev/null -q || echo -ne "\n====\nERROR H3 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int2-h4 1>/dev/null -q || echo -ne "\n====\nERROR H4 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int2-h5 1>/dev/null -q || echo -ne "\n====\nERROR H5 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int2-h6 1>/dev/null -q || echo -ne "\n====\nERROR H6 de la subred INTNet2 \n==== \n";
ping -c1 -w 2 int4-h1 1>/dev/null -q || echo -ne "\n====\nERROR H1 de la subred INTNet4 \n==== \n";
ping -c1 -w 2 int4-h2 1>/dev/null -q || echo -ne "\n====\nERROR H2 de la subred INTNet4 \n==== \n";
ping -c1 -w 2 int4-s1 1>/dev/null -q || echo -ne "\n====\nERROR S1 de la subred INTNet4 \n==== \n";
ping -c1 -w 2 dmz-s1 1>/dev/null -q || echo -ne "\n====\nERROR S1 de la subred DMZ \n==== \n";
ping -c1 -w 2 dmz-ms 1>/dev/null -q || echo -ne "\n====\nERROR MS de la subred DMZ \n==== \n";
