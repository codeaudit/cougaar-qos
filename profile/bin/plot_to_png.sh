#!/bin/sh

# plot a csv to a png file

if [ -z $1 ]; then
  cat << EOF
Usage: $0 CSV_FILE [TITLE] [XAXIS_LABEL] [YAXIS_LABEL]

Assumes the first column is the x axis (e.g. line number)
and the first row is the line name for each column.

Example use:
  $0 my_data.csv "My Graph" "time" "value"

Example input file:
  0,x,y
  1,3,5
  2,9,25
EOF
  exit 1
fi

file=$1
title=$2
xaxis=$3
yaxis=$4

maxtitle=25

inputfile=tmp_input
gnufile=tmp_gnu
outputfile=`echo $file | sed -e 's/^\(.*\)\([^\/]*\)\.csv$/\1\2.png/'`

#if [ -f $outputfile ]; then
#  echo "Output png file already exists: $outputfile"
#  exit 1
#fi

cols=0
declare names
for x in `\
  head -1 $file |\
  sed -e 's/,[ ]*$//' |\
  sed -e 's/,/ /g'\
  `; do
  name=`echo $x | sed -e 's/^TWRIGHT-//'`
  names[$cols]=$name
  let cols=$cols+1
done

len=`wc -l $file | sed -e 's/^[ ]*\([0-9]*\)[^0-9]*.*$/\1/'`
let len2=$len
let len=$len-1
tail -$len $file | tr ',' ' ' > $inputfile
#let len2=$len-110
#let len=$len2-1
#head -$len2 $file | tail -$len | tr ',' ' ' > $inputfile


# gnuplot dies if there are too many key titles,
# so pick out the max 5 and trim 'em a bit.
#firstlines=`perl ./get_max_indexes.plx $file`
firstlines=`head -$len2 $file | perl ./get_max_indexes.plx`

cat > ${gnufile} <<EOF

set output "$outputfile"
set terminal png color
set size 0.65, 0.65   # scale 640x480 down

EOF
if [ "" != "$title" ]; then
  echo "set title \"${title}\"" >> ${gnufile}
fi
if [ "" != "$xaxis" ]; then
  echo "set xlabel \"${xaxis}\"" >> ${gnufile}
fi
if [ "" != "$yaxis" ]; then
  echo "set ylabel \"${yaxis}\"" >> ${gnufile}
fi
cat >> ${gnufile} <<EOF

# lines, no points:
set data style lines
# points
#set data style points
# histogram:
#set data style boxes
#set boxwidth 1

set grid
set noxtics
set key box
#set nokey

plot \\
EOF

# first, print the lines with max values (in order) and
# list them in the key.
declare titles
for x in $firstlines; do
 titles[$x]=1
done
count=0
for i in $firstlines; do
  let j=$i+1
  echo -n " \"${inputfile}\" using 1:$j" >> ${gnufile}
  ttl=${names[$i]}
  trim=`echo $ttl | cut -c1-$maxtitle`
  if [ $ttl != $trim ]; then
    ttl="${trim}.."
  fi
  echo -n " title \"${ttl}\"" >> ${gnufile}
  let count=$count+1
  if [ $count -lt $cols ]; then
    echo ", \\" >> ${gnufile}
  else 
    echo "" >> ${gnufile}
  fi
done

# next, print the rest of the lines without titles
if [ $count -lt $cols ]; then
  i=1
  while [ true ]; do
    let j=$i+1
    if [ ! -z ${titles[$i]} ]; then
      let i=$j
      continue
    fi
    echo -n " \"${inputfile}\" using 1:$j" >> ${gnufile}
    echo -n " notitle" >> ${gnufile}
    if [ $j -lt $cols ]; then
      echo ", \\" >> ${gnufile}
    else
      echo "" >> ${gnufile}
      break
    fi
    let i=$j
  done
fi

gnuplot $gnufile

#rm $gnufile $inputfile
