#!/usr/bin/perl

#
# This is an ugly super-script that parses "*.log" for 28 metrics
# logging output categories:
#    cpu_loadavg
#    cpu_user
#    thread_loadavg
#    p(sum|dir)
#    blackboard_(count|add|change|remove|operations)
#    Task_('')
#    transaction_('')
#    msg_(bytes|count)_(sent|recv)
#    msg_histogram
#    network_(eth0|eth1)_(sent|recv)
# and creates 4 CSVs per category:
#    count
#    "_all"   ("Total" sum for all columns)
#    "_rate"  (per-line delta of count)
#    "all_rate"  (rate for "_all")
# then creates PNG images for each CSV.
# This totals 4*28 = 112 PNGs
#

my %handlers;

my %node_to_host;

parse_args();
add_handlers();
read_files();
post_process();
write_files();
exit(0);

sub usage {
# FIXME
  print STDERR <<EOF;
Usage: $0
EOF
  exit(1);
}

sub parse_args {
}

sub add_handlers {
  print STDERR "loading handlers:\n";
  # comment out any of these!
  track_cpu_loadavg();
  track_cpu_user();
  track_thread_loadavg();
  track_persistence();
  track_blackboard();
  track_msg();
  track_msg_histogram();
  track_network();
  track_traffic_matrix();
}
sub track_cpu_loadavg {
  # cpu loadavg
  my %h = (
    "type" => "loadavg",
    "per_host" => "1",
    "title" => "CPU Load Average",
    "yaxis" => "Load");
  add_handler(\%h);
}
sub track_cpu_user {
  # cpu /proc user jiffies
  my %h = (
    "type" => "cpu",
    "per_host" => "1",
    "prefix" => "cpu_user",
    "title" => "CPU User Mode",
    "yaxis" => "Jiffies (1/100th seconds)");
  add_handler(\%h);
}
sub track_thread_loadavg {
  # cougaar thread loadavg
  my %h = (
    "type" => "resources",
    "column" => 1,
    "prefix" => "thread_loadavg",
    "title" => "Cougaar ThreadService Load Average",
    "yaxis" => "Load");
  add_handler(\%h);
}
sub track_persistence {
  # cougaar disk persistence
  foreach $level ('host', 'node', 'agent') {
    #foreach $level ('host') {}
    my $t;
    if ($level eq "agent") {
      $t="pdir";
    } else {
      $t="psum";
    }
    my $sum_host=0;
    if ($level eq "host") {
      $sum_host=1;
    }
    my %h = (
      "type" => $t,
      "sum_host" => $sum_host,
      "prefix" => "persistence_${level}",
      "title" => "Persistence Files",
      "yaxis" => "Bytes");
    add_handler(\%h);
  }
}
sub track_blackboard {
  # cougaar blackboard
  foreach $t ('UniqueObject', 'Task', 'transactions') {
    foreach $column ('count', 'add', 'change', 'remove', 'operations') {
      my $title=$t;
      my $type=$t;
      if ($t eq "UniqueObject") {
        $type="blackboard";
        $title="Blackboard";
      } elsif ($t eq "transactions") {
        if ($column ne "count") {
          next;
        }
        $title="Transaction";
      } else {
      }
      my $c;
      if ($column eq "count") {
        $c = 0;
        if ($t eq "UniqueObject") {
          $title="$title Size";
        } else {
          $title="$title Count";
        }
      } elsif ($column eq "add") {
        $c = 1;
        $title="$title 'publishAdd' Operations";
      } elsif ($column eq "change") {
        $c = 2;
        $title="$title 'publishChange' Operations";
      } elsif ($column eq "remove") {
        $c = 3;
        $title="$title 'publishRemove' Operations";
      } elsif ($column eq "operations") {
        $c = 4;
        $title="$title 'publish\*' Operations";
      }
      my %h = (
        "type" => $t,
        "column" => $c,
        "prefix" => "${type}_${column}",
        "title" => $title);
      add_handler(\%h);
    }
  }
}
sub track_msg {
  # cougaar messages
  foreach $level ('host', 'node', 'agent') {
    #foreach $level ('host') {}
    foreach $n ('bytes', 'count') {
      foreach $d ('send', 'recv') {
        my $c;
        my $t;
        my $y;
        my $type;
        if ($level eq "agent") {
          $type = "tl";
        } else {
          $type = "nt";
        }
        if ($n eq "bytes") {
          if ($level eq "agent") {
            $c=1;
          } else {
            $c=4;
          }
          $t="Message Bytes";
          $y="Bytes";
        } else {
          if ($level eq "agent") {
            $c=0;
          } else {
            $c=6;
          }
          $t="Messages";
          $y="Count";
        }
        if ($d eq "send") {
          $p="sent";
          $t="$y Sent";
        } else {
          unless ($level eq "agent") {
            $c+=6;
          }
          $p="recv";
          $t="$y Received";
        }
        my $sum_host=0;
        if ($level eq "host") {
          $sum_host=1;
        }
        my %h = (
          "type" => $type,
          "sum_host" => $sum_host,
          "column" => $c,
          "prefix" => "msg_${n}_${p}_${level}",
          "title" => $t,
          "yaxis" => $y);
        add_handler(\%h);
      }
    }
  }
}
sub track_msg_histogram {
  # cougaar message size histogram
  my %h = (
    "type" => "nt",
    "column" => 11,
    "prefix" => "msg_histogram",
    "title" => "Message Size Histogram",
    "xaxis" => "Bytes");
  add_handler(\%h);
}
sub track_network {
  # network bytes sent/received
  foreach $n ('eth0') {
    #foreach $n ('eth0', 'eth1') {}
    foreach $d ('sent', 'recv') {
      my $c;
      my $t;
      if ($d eq "sent") {
        $c=8;
        $t="Sent";
      } else {
        $c=0;
        $t="Received";
      }
      my %h = (
        "type" => $n,
        "per_host" => "1",
        "column" => $c,
        "prefix" => "network_${n}_${d}",
        "title" => "Network Bytes ${t} (${n})",
        "yaxis" => "Bytes",
        "leadzeros" => 1,
        "baseline" => 1);
      add_handler(\%h);
    }
  }
}
sub track_traffic_matrix {
  # cougaar message traffic matrix
  foreach $n ('count', 'bytes', 'connect') {
    my $c;
    if ($n eq "count") {
      $c=0;
    } else {
      $c=1;
    }
    my %h = (
      "type" => "tm",
      "column" => $c,
      "prefix" => "traffic_matrix_${n}");
    add_handler(\%h);
  }
}

sub add_handler {
  my ($href) = @_;

  my $key=$href->{"prefix"};
  if ($key eq "") {
    $key=$href->{"type"};
  }
  print STDERR "  $key\n";

  my %newHash;
  my $hashRef = \%newHash;
  $handlers{$key} = $hashRef;

  foreach $k (keys %$href) {
    $hashRef->{$k} = $href->{$k};
  }
  if ($hashRef->{"prefix"} eq "") {
    $hashRef->{"prefix"}=$href->{"type"};
  }
  if ($hashRef->{"xaxis"} eq "") {
    $hashRef->{"xaxis"}="Time (Minutes)";
  }
  if ($hashRef->{"yaxis"} eq "") {
    $hashRef->{"yaxis"}="Count";
  }

  my %h2;
  my $h2Ref = \%h2;
  $hashRef->{"agents"} = $h2Ref;
}

sub read_files {
  print STDERR "reading files:\n";

  # list "*.log"
  opendir(DIR, ".");
  @files = grep(/\.log$/, readdir(DIR));
  closedir(DIR);

  foreach $file (sort @files) {
    open(FD, "<$file")
      or die "Unable to open $file: $!\n";
    my $agent = $file;
    if ($agent =~ /^(.*\/)?([^\/]*)\.log$/) {
      $agent = $2;
    }
    my $got_host=0;
    print STDERR "  $file\n";
    while (<FD>) {
      my $line = $_;
      chomp;
      if ($got_host == 0 &&
        $line =~
        /^
        .*
        \s+
        AgentLifecycle
        .*
        \s+
        Node\(([^\)]+)\)
        \s+
        Host\(([^\)]+)\)
        .*
        $/x) {
        $node = $1;
        $host = $2;
        $node_to_host{$node} = $host;
        $got_host = 1;
        next;
      }
      next unless 
        $line =~ 
          /^(.*)
          \s+(\-\s+|\[)
          ([^\[\]]+)
          \]*
          \s+-\s+
          ([-0-9\.,\s]+)
          $/x;
      my $type = $3;
      my @cols = split(',', $4);
      next if $tag eq "STDOUT";
      read_line($agent, $type, \@cols);
    }
    close FD;
  }
}

sub read_line {
  my ($agent, $type, $colsRef) = @_;
  foreach $key (keys %handlers) {
    my $hashRef = $handlers{$key};
    my $keyType = $hashRef->{"type"};
#    if ($keyType eq "psum") {
#      $keyType = "psum_${agent}";
#    }
    my $bb = 0;
    my $tm = 0;
    my $target = "";
    if ($type ne $keyType) {
      if (($keyType eq "pdir" ||
           $keyType eq "psum" ||
           $keyType eq "tl") &&
         $type =~ /${keyType}_(.*)/) {
        $agent = $1;
      } elsif ($keyType eq "tm" && $type =~ /tm_(.*)__to__(.*)/) {
        $agent = $1;
        $target = $2;
        $tm = 1;
      } elsif ($type =~ /bb_(.*)__${keyType}/) {
        $agent = $1;
        $bb = 1;
      } else {
        next;
      }
    }

    my $agentsRef = $hashRef->{"agents"};
    my $col = $hashRef->{"column"};

    if ($tm == 1) {
      if ($hashRef->{"prefix"} eq "traffic_matrix_connect") {
        # ignore the connectivity matrix, which is simply
        #   val[agent,target] = (count[agent,target] > 0 ? 1 : 0);
        next;
      }
      my $hr = $agentsRef->{$agent};
      if (!defined($hr)) {
        my %newH;
        $hr = \%newH;
        $agentsRef->{$agent} = $hr;
      }
      $hr->{$target} = $colsRef->[$col];
      next;
    }

    my $arrRef = $agentsRef->{$agent};
    if (!defined($arrRef)) {
      my @newArray = [];
      $arrRef = \@newArray;
      $agentsRef->{$agent} = $arrRef;
      if ($hashRef->{"leadzeros"} == 1) {
        $hashRef->{"firstZeros"} = 1;
      }
    }

    if ($type eq "nt" && $col == 11) {
      # msg histogram
      my $len = @$colsRef-12;
      for (my $j = 0; $j < $len; $j++) {
        $arrRef->[$j]=$colsRef->[$j+11];
      }
      next;
    }

    my $val;
    if ($bb == 1 && $col == 4) {
      $val = $colsRef->[1]+$colsRef->[2]+$colsRef->[3];
    } elsif ($type eq "resources") {
      if ($col == 11) {
        # host memory, in kB
        $val = 1000*($colsRef->[8] - $colsRef->[9]);
      } else {
        $val = $colsRef->[$col];
        if ($col == 8 || $col == 9) {
          $val *= 1000;
        }
      }
    } else {
      $val = $colsRef->[$col];
    }

    my $firstZeros = $hashRef->{"firstZeros"};
    if ($firstZeros == 1) {
      my $leadvalue = $hashRef->{"leadvalue"};
      if ($val != $leadvalue) {
        $firstZeros = 0;
        $hashRef->{"firstZeros"} = 0;
        my $len = @$arrRef;
        for (my $j = 0; $j < $len; $j++) {
          $arrRef->[$j]=$val;
        }
      }
    }
    push(@$arrRef, $val);
  }
}

sub post_process {
  # merge per-host columns
  #
  # E.g.
  #   host "h1" contains nodes "n1a" and "n1b"
  #   host "h2" contains node "n2"
  # the (per-host) /proc/loadavg table is:
  #   n1b, n1a, n2
  #   0.4, 0.4, 0.1
  #   0.2, 0.2, 0.9
  # where "n1a" and "n1b" are identical.
  # Merge into:
  #   n1a+n1b, n2
  #   0.4,     0.1
  #   0.2,     0.9
  # Note that we use "n1a+n1b" instead of "h1", since the
  # node names are typically more useful than the host names.
  #
  # we could do this as we read the files, but for now
  # it's implemented separately...

  # see if there are any "per_host" handlers
  my $any_match = 0;
  foreach $key (keys %handlers) {
    my $hashRef = $handlers{$key};
    if ($hashRef->{"per_host"} == 0 ||
      $hashRef->{"sum_host"} == 1) {
      $any_match = 1;
      last;
    }
  }
  return unless $any_match == 1;

  # map host to node names array
  # e.g. "h1" => ("n1b", "n1a")
  my %host_to_nodes;
  foreach $node (sort keys %node_to_host) {
    my $host = $node_to_host{$node};
    printf STDERR "node_to_host{$node}=$host\n";
    my $ar = $host_to_nodes{$host};
    if (!defined($ar)) {
      my @a;
      $ar=\@a;
      $host_to_nodes{$host}=$ar;
    }
    push(@$ar, $node);
  }
  # map host to name
  # e.g. "h1" => "n1a+n1b"
  my %host_to_name;
  foreach $host (sort keys %host_to_nodes) {
    my $ar = $host_to_nodes{$host};
    my $name = "";
    my $n = @$ar;
    if ($n == 0) {
      next;
    } elsif ($n == 1) {
      $name = $ar->[0];
    } else {
      my $len = (25 / $n) - 3;
      my @nodes = sort @$ar;
      for (my $i = 0; $i < $n; $i++) {
        my $node = $nodes[$i];
        if ($node =~ /^TWRIGHT-(.*)$/) {
          $node = $1;
        }
        my $trim = $node;
        if (length($node) > $len) {
          $trim = substr($node, 0, $len);
          $trim = "$trim..";
        }
        if ($i == 0) {
          $name = $trim;
        } else {
          $name = "$name+$trim";
        }
      }
    }
    printf STDERR "host_to_name{$host}=$name\n";
    $host_to_name{$host}=$name;
  }
  # map node to name
  # e.g. "n1a" => "n1a+n1b"
  my %node_to_name;
  foreach $node (keys %node_to_host) {
    my $host = $node_to_host{$node};
    my $name = $host_to_name{$host};
    $node_to_name{$node}=$name;
    printf STDERR "node_to_name{$node}=$name\n";
  }

  # fix data columns
  foreach $key (keys %handlers) {
    my $hashRef = $handlers{$key};
    my $per_host;
    if ($hashRef->{"per_host"} == 1) {
      $per_host=1;
    } elsif ($hashRef->{"sum_host"} == 1) {
      $per_host=0;
    } else {
      next;
    }

    my $keyType = $hashRef->{"type"};
    my $agentsRef = $hashRef->{"agents"};

    my %newAgentsHash;
    my $newAgentsRef = \%newAgentsHash;
    foreach $agent (keys %$agentsRef) {
      my $name = $node_to_name{$agent};
      if (!defined($name)) {
        # unknown agent?
        $name = $agent;
      }
      #print STDERR "newAgentsRef->{$name} = agentsRef->{$agent}\n";
      my $arrRef = $agentsRef->{$agent};
      my $newArrRef;
      if ($per_host == 0) {
        $newArrRef = $newAgentsRef->{$name};
      }
      if ($per_host == 1 || !defined($newArrRef)) {
        # just keep one column
        $newAgentsRef->{$name} = $arrRef;
        next;
      }
      # sum columns
      my $n = @$newArrRef;
      for (my $i = 0; $i < $n; $i++) {
        $newArrRef->[$i] += $arrRef->[$i];
      }
    }
    $hashRef->{"agents"} = $newAgentsRef;
  }
}

sub write_files {
  print STDERR "writing files:\n";

  foreach $key (keys %handlers) {
    foreach $delta (0, 1) {
      foreach $sum (0, 1) {
        # get filename base
        my $d = "";
        if ($delta == 1) {
          $d="_rate";
        }
        my $s = "";
        if ($sum == 1) {
          $s="_all";
        }
        my $hashRef = $handlers{$key};
        my $prefix=$hashRef->{"prefix"};
        my $file="$prefix$s$d";

        # write files
        print STDERR "  $file";
        write_csv($file, $key, $delta, $sum);
        write_png($file, $key, $delta, $sum);
        print STDERR "\n";
      }
    }
  }
}

sub write_csv {
  my ($file, $key, $delta, $sum) = @_;

  print STDERR " (csv)";

  open(FD, ">$file.csv")
    or die "Unable to open $file: $!\n";

  my $hashRef = $handlers{$key};

  my $type = $hashRef->{"type"};

  my $tm = 0;
  my $tm_connect = 0;
  if ($type eq "tm") {
    $tm = 1;
    if ($hashRef->{"prefix"} eq "traffic_matrix_connect") {
      $tm_connect = 1;
      $hashRef = $handlers{"traffic_matrix_count"};
    }
  }

  my $agentsRef = $hashRef->{"agents"};

  my @keys = sort keys %$agentsRef;

  print FD "0, ";
  if ($sum == 0) {
    foreach (@keys) {
      my $agent = $_;
      if ($agent =~ /^TWRIGHT-(.*)$/) {
        $agent = $1;
      }
      print FD "$agent, ";
    }
  } else {
    print FD "Total, ";
  }
  print FD "\n";

  if ($tm == 1) {
    # traffic matrix
    foreach (@keys) {
      my $agent = $_;
      print FD "$agent, ";
      my $hr = $agentsRef->{$agent};
      if (!defined($hr)) {
        # never?
        my %newH;
        $hr = \%newH;
        $agentsRef->{$agent} = $hr;
      }
      my $vsum = 0;
      foreach (@keys) {
        my $target = $_;
        my $v = $hr->{$target};
        if (!defined($v)) {
          $v=0;
        } elsif ($tm_connect == 1) {
          if ($v > 0) {
            $v = 1;
          }
        }
        if ($sum == 0) {
          print FD "$v, ";
        }
        if ($v > 0) {
          $vsum += $v;
        }
      }
      if ($sum == 1) {
        print FD "$vsum, ";
      }
      print FD "\n";
    }
    close FD;
    return;
  }

  my $col = $hashRef->{"column"};

  # get any array's length
  my $anyKey=$keys[0];
  my $lenArrRef = $agentsRef->{$keys[0]};
  my $len = @$lenArrRef;

  # FIXME off by one somehow?
  $len--;

  for (my $i = 1; $i <= $len; $i++) {
    if ($type ne "nt" || $col != 11) {
      print FD "$i, ";
    } else {
      # histogram buckets: 100, 200, 500, 1000, ..
      my $j = $i - 1;
      my $m = ($j % 3);
      my $d = (($j - $m)/3);
      my $f = 1;
      if ($m == 1) {
        $f = 2;
      } elsif ($m == 2) {
        $f = 5;
      }
      my $bucket=$f*(10**(2+$d));
      print FD "$bucket, ";
    }
    my $vsum = 0;
    foreach (@keys) {
      my $agent = $_;
      my $arrRef = $agentsRef->{$agent};
      my $value = $arrRef->[$i];
      if ($delta == 1) {
        if ($i == 1) {
          $value = 0;
        } else {
          $value -= $arrRef->[$i - 1];
        }
      } elsif ($hashRef->{"baseline"} == 1) {
        $value -= $arrRef->[1];
      }
      if ($sum == 0) {
        print FD "$value, ";
      }
      $vsum += $value;
    }
    if ($sum == 1) {
      print FD "$vsum, ";
    }
    print FD "\n";
  }

  close FD;
}

sub write_png {
  my ($file, $key, $delta, $sum) = @_;

  my $hashRef = $handlers{$key};

  if ($hashRef->{"type"} eq "tm") {
    # skip traffic matrix!
    return;
  }

  print STDERR " (png)";

  my $xaxis=$hashRef->{"xaxis"};
  my $yaxis=$hashRef->{"yaxis"};
  my $title=$hashRef->{"title"};
  if ($delta == 1) {
    $title="$title (Rate)";
  }
  `./plot_to_png.sh $file.csv "$title" "$xaxis" "$yaxis"`
}
