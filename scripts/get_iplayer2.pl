#!/usr/bin/perl
use File::Basename;
 
my $pid=$ARGV[1];
my $dir=dirname($ARGV[0]);
my $mode = "flashhigh1,flashstd1,flashvhigh1";
my $data=`extras\\perl\\bin\\perl $dir\\get_iplayer.pl --pid ${pid} --mode=${mode} --streaminfo`;

my $surl='';
my $swf='';
my $pp='';
my $app='';
if($data =~ m/streamurl:\s+(.*)/i) {
  $surl=$1;
}
if($data =~ m/swfurl:\s+(.*)/i) {
  $swf=$1;
}
if($data =~ m/playpath:\s+(.*)/i) {
  $pp=$1;
}
if($data =~ m/application:\s+(.*)/i) {
  $app=$1;
}

print "RTMPDUMP!!!-r=".$surl."!!!swfVfy=".$swf."!!!-y=".$pp."!!!-a=".$app;
