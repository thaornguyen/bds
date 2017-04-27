#bash
dir="/root/"
project="/root/bds"
local_data=/home/thaonp/Desktop/zitec_data/
# for i in {02..11}
for i in {02..05}
do
    echo "ssh to jp $i"
    # ssh jp$i "cd $dir && git clone https://github.com/thaornguyen/bds.git"
    # ssh jp$i "cd $project && git pull"
    # ssh jp$i "cd $project && git pull && ./gradlew build"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t prod -i data/zitec_2/zitec.cate.$i.txt"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t prod-info -i data/zitec/zitec.prod.$i.txt"
    # ssh  jp$i "pkill -f ZitecCrawler"
    # ssh  jp$i "ps aux | grep [j]ava"
    # ssh  jp$i "cd $project && rm -fr /root/bds/data/zitec_2/zitec.prod.* && rm -fr /root/bds/data/zitec_2/zitec.cate.error.txt"
    # ssh  jp$i "cd $project && rm -fr /root/bds/data/zitec/*.tsv"
    # ssh  jp$i "cd $project/data/zitec/ && unzip zitec.prod.link.tsv.zip"
    # ssh  jp$i "cd $project && grep -c PROD-OK logs/logging.log"
    # ssh  jp$i "cd $project && tail -n 5 logs/logging.log"
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log | grep NR| grep REQUEST_TIMEOUT" 
    # ssh  jp$i "cd $project && grep FINISH logs/logging.log" 
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.link.tsv && wc -l data/zitec/zitec.prod.tsv"
    # ssh  jp$i "cd $project && cat data/zitec_2/zitec.cate.error.txt"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.cate.error.txt"
    mkdir -p $local_data/data_0426_02/$i
    scp -r jp$i:/root/bds/data/zitec/zitec.prod.tsv $local_data/data_0426_02/$i
done


# for i in {02..04}
# do
#     echo "ssh to jp $i"
    # ssh jp$i "cd $dir && git clone https://github.com/thaornguyen/bds.git"
    # ssh jp$i "cd $project && git pull"
    # ssh jp$i "cd $project && git pull && ./gradlew build"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t prod -i data/zitec/zitec.cate.$i.txt"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t cate-err"
    # ssh  jp$i "pkill -f ZitecCrawler"
    # ssh  jp$i "ps aux | grep [j]ava"
    # ssh  jp$i "cd $project && rm -fr data"
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log | grep NR| grep REQUEST_TIMEOUT" 
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log.2017-04-20" 
    # ssh  jp$i "cd $project && grep FINISH logs/logging.log" 
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.link.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.cate.error.txt"
# done
# for i in {06..14}
# do
    # echo "ssh to jp $i"
    # ssh jp$i "cd $dir && git clone https://github.com/thaornguyen/bds.git"
    # ssh jp$i "cd $project && git pull"
    # ssh jp$i "cd $project && git pull && ./gradlew build"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t prod -i data/zitec/zitec.cate.$i.txt"
    # ssh -f jp$i "cd $project && java -jar build/libs/ZitecCrawler-1.0.jar -c conf/zitec$i.properties -t cate-err"
    # ssh  jp$i "pkill -f ZitecCrawler"
    # ssh  jp$i "ps aux | grep [j]ava"
    # ssh  jp$i "cd $project && rm -fr data"
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log | grep NR| grep REQUEST_TIMEOUT" 
    # ssh  jp$i "cd $project && grep ERROR logs/logging.log.2017-04-20" 
    # ssh  jp$i "cd $project && grep FINISH logs/logging.log" 
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.link.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.prod.tsv"
    # ssh  jp$i "cd $project && wc -l data/zitec/zitec.cate.error.txt"
# done