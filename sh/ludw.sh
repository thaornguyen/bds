#bash
dir="/root/"
project="/root/ludw"

local_data=/home/thaonp/Desktop/ludw_data/data_2704/
# for i in {02..11}
for i in {02..15}
do
    echo "ssh to jp $i"
    # ssh jp$i "cd $dir && git clone https://github.com/thaornguyen/bds.git ludw"
    # ssh jp$i "cd $project && git pull"
    # ssh jp$i "cd $project && git pull && ./gradlew build"
    # ssh -f jp$i "cd $project && java -jar build/libs/LudwCrawler-1.0.jar -t prod-info -i data/ludw/ludw.prod.link.$i.txt"
    # ssh  jp$i "cd $project && wc -l data/ludw/ludw.prod.link.tsv && wc -l data/ludw/ludw.prod.tsv"
    # ssh  jp$i "cd $project/data/ludw && cat ludw.prod.link.tsv | cut -f 2 | sort | uniq > test.link.txt && wc -l test.link.txt"
    #  ssh  jp$i "cd $project/data/ludw && cat ludw.prod.tsv | cut -f 1 | sort | uniq > test.prod.txt && wc -l test.prod.txt"
    # ssh  jp$i "pkill -f LudwCrawler"
    # ssh  jp$i "ps aux | grep [j]ava"
    # ssh  jp$i "cd $project && rm -fr data/ludw/ludw.prod.tsv"
    # ssh  jp$i "cd $project && rm -fr /root/ludw/build"
    # ssh  jp$i "cd $dir && rm -fr /root/ludw/data/logs && rm -fr /root/ludw/data/ludw/ludw.prod*"
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log" 
    # ssh  jp$i "cd $project && wc -l data/ludw/ludw.prod.tsv" 
    # ssh  jp$i "cd $project && tail -n 5 logs/logging.log" 
    # ssh  jp$i "cd $project && grep FINISH logs/logging.log" 
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.link.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.tsv"
    # ssh  jp$i "cd $project && wc -l data/ludw/ludw.prod.link.*"

    mkdir -p $local_data/$i
    scp -r jp$i:/root/ludw/data/ludw/ludw.prod.tsv $local_data/$i
done


