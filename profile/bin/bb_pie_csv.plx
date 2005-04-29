#!/usr/bin/perl


my %types;
my $col=0;

parse_args();
process_files();
exit(0);

sub usage {
  print STDERR <<EOF;
Usage: $0 ACTION

Parse Cougaar "*.log" files to create a CSV of blackboard
activity, with a column per agent (plus a "Total" column) and
rows per blackboard object type.

Valid ACTIONs are:
  count          (current count)
  add            (publishAdd's)
  change         (publishChange's)
  remove         (publishRemove's)
  operations     (add+change+remove)

Example log line:
2004-12-21 18:46:53,014 SHOUT - bb_USAEUR_MIL__UniqueObject - 96, 101, 161, 5
EOF
  exit(1);
}

sub parse_args {
#  if ($#ARGV < 2) {
#    usage();
#  }
  $col = shift @ARGV;
  if ($col =~ /"^[0-9]+$"/) {
    if ($col < 0 || $col > 4) {
      usage();
    }
  } else {
    if ($col eq "count") {
      $col = 0;
    } elsif ($col eq "add") {
      $col = 1;
    } elsif ($col eq "change") {
      $col = 2;
    } elsif ($col eq "remove") {
      $col = 3;
    } elsif ($col eq "operations") {
      $col = 4;
    } else {
      usage();
    }
  }
}

sub process_files {
  opendir(DIR, ".");
  @files = grep(/\.log$/, readdir(DIR));
  closedir(DIR);

  my $len = -1;
  foreach $file (@files) {
    open(FD, "<$file")
      or die "Unable to open $file: $!\n";
    my $i = 0;
    while (<FD>) {
      my $line = $_;
      chomp;
      next unless 
        /^(.*)\s+\[?
        bb_(.*)__([^\]]*)\]?\s+-\s+
        ([-0-9\.,\s]+)
        $/x;
      my $agent = $2;
      my $type = $3;
      my @cols = split(',', $4);
      my $value = 0;
      if ($col >= 0 && $col < 4) {
        $value = $cols[$col];
      } else {
        $value = $cols[1]+$cols[2]+$cols[3];
      }
      my $hashRef = $agents{$agent};
      if (!defined($hashRef)) {
        my %newHash;
        $hashRef = \%newHash;
        $agents{$agent} = $hashRef;
      }
      $hashRef->{$type} = $value;
    }
    close FD;
  }

  my @keys = sort keys %agents;
  print "0, Total, ";
  foreach (@keys) {
    my $agent = $_;
    print "$agent, ";
  }
  print "\n";

  my %sum;
  foreach (@keys) {
    my $agent = $_;
    my $hashRef = $agents{$agent};
    foreach (keys %$hashRef) {
      my $type = $_;
      my $value = $hashRef->{$type};
      $sum{$type} += $value;
    }
  }
  my @types = sort keys %sum;

  foreach (@types) {
    my $type = $_;
    my $tsum = $sum{$type};
    print "$type, $tsum, ";
    foreach (@keys) {
      my $agent = $_;
      my $hashRef = $agents{$agent};
      my $value = $hashRef->{$type};
      print "$value, ";
    }
    print "\n";
  }
}
