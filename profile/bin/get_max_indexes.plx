#!/usr/bin/perl

my $maxcount=5;
my $print_name=0;
my $print_index=1;

my $init=0;
my @names;
my @max;
while (<>) {
  chomp;
  my @cols=split(',');
  if ($init == 0) {
    $init = 1;
    foreach $col (@cols) {
      if ($col =~ /^\s*(\S+)\s*$/) {
        $col=$1;
        push(@names, $col);
        push(@max, 0);
      }
    }
    next;
  }
  for (my $i = 1; $i <= @names; $i++) {
    my $v = $cols[$i];
    if ($v >= $max[$i]) {
      $max[$i] = $v;
    }
  }
}

my %index;
my %max;
for (my $i = 1; $i <= @names; $i++) {
  my $name = $names[$i];
  $index{$name} = $i;
  $max{$name} = $max[$i];
}
my $count=0;
foreach (reverse sort {$max{$a} <=> $max{$b}} @names) {
  my $name = $_;
#  print STDERR "max[$count]=${index{$name}} ($name)\n";
  if ($print_index == 1) {
    print "${index{$name}}\n";
  } elsif ($print_name == 1) {
    print "$name\n";
  }
  last if (++$count >= $maxcount);
}
