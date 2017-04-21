# Chapter 01 하둡과의 만남

# 1.1 데이터
공개적으로 이용할 수 있는 데이터 역시 매년 증가하고 있다. 조직은 더 이상 자신들의 데이터로만 정보를 도출하면 안된다. 다른 조직의 정보 또한 이용할 수 있어야 한다.

 - ![한국 정부3.0 공공 데이터](https://www.data.go.kr)
 - ![네이버 데이터 랩](http://datalab.naver.com/opendata.naver)
 - ![아마존 웹 서비스 공공 데이터 셋](http://aws.amazon.com/publicdatasets/)
 - ![Infochimps.org](http://infochimps.org/)
 - ![theinfo.org](http://theinfo.org/)

데이터는 널려있지만 분석하기위한 노력은 좋은 알고리즘 찾는것 보다 배가될 것이다.

# 1.2 데이터 저장소와 분석
### 분산처리의 이점
 - 용량 100GB의 하드디스크를 전부 읽기위해서는 2시간 반이 걸린다.
 - 하지만 용량 100GB의 하드디스크에서 1GB만 사용해서 읽을 때에는 2분이 걸린다.
 - 100GB의 용량에 100GB의 파일을 저장하여 전부를 읽을 때 100GB 디스크 하나에 전부를 저장할것인가. 아니면 100개의 HDD 에 1GB씩 저장할것인가?
 - 메모리적 효율을 따진다면 하나에 100GB를 저장
 - 빠른 결과값을 원한다면 100개의 HDD에 1GB씩 저장하여 병렬처리하여 읽을 것이다.

병렬처리 및 분산처리는 이런 장점을 가지고 있다. 하지만 이러한 분산처리는 여러가지 고려해야할 사항을 가지고 있다. 

### HDD로 부터 병렬로 읽고 쓰기위한 문제점
 1. 하드웨어 장애 : 오래 사용하면 사용할 수록 장애가 발생할 확률이 높아지게 된다. 손상이 발생하게되면 저장되어있던 데이터는 사용하지 못하게 됨으로 백업 체계를 필요하게 된다.
   - RAID의 병렬 디스크 이용 : RAID의 방식에따라 HDD가 손상이 되었을 때 복구하는 방법이 다르다. 그렇기 때문에 hdfs는 RAID의 데이터 복구 프로세스를 이용하여 작동한다.
 2. 대부분의 분석 작업은 분할된 데이터를 어떤 식으로든 결합할 필요가 있다. 
   - 분산된 HDD에서 저장했던 파일을 불러오기위해 100개의 디스크로부터 읽은 데이터를 병합할 필요가 있다. hdfs는 이 데이터를 불러올 때 MapReduce계산을 이용하여 리소스를 읽어온다. 

# 1.3 hdfs의 특징
 - 맵 리듀스는 일괄처리기
 - 전체 데이터를 대상으로 ad-hoc 쿼리를 수행하면서 합리적인 시간 내에 결과를 보여준다. (빠르다. 그러나 spark 보다느리다.  hdfs 를 이용하기때문에 느림.)
 - 맵리듀스는 애드혹 분석을 위해 일괄 처리 방식으로 전체 데이터셋을 분석할 필요가 있는 문제에 적합
 - 맵리듀스는 데이터를 한 번 쓰면 여러 번 읽는 응용 프로그램에 적합하다.
 
# 맵 리듀스의 특징
 - 정규화된 데이터셋은 멥리듀스의 데이터 레코드 셋으로 부적합.
 - 맵리듀스는 선형적으로 확장할 수 있는 프로그래밍 모델.
 - 맵리듀스가 빛을 발하는 순간은 수백기가바이트의 데이터를 엑세스 하는 경우
 - 맵리듀스 구현은 네트워크 대역폭을 보존하기 위해 네트워크 위상을 명시적으로 모델링 할 수 있다.
 - 테스크들중에 부분장애(partial failure)가 발생하면 실패한 태스크만 재스캐줄링 한다. 
 - 맵 리듀스가 태스크 간에 상호 의존성이 없는 '무공유 아키텍처' 이기 때문에 가능하다.
   > 맵퍼의 출력은 리듀서로 전송되는데, 태스크가 상호 의존성이 없다는 전제로 장애를 처리하는 방식은 지나친 감이 있다. 
   > 오히려 실패한 맵보다는 실패한 리듀서를 원상복구하는 데에 더 주의해야하는 데, 필수적인 맵 출력을 반드시 회수할 수 있어야 하고, 만일 아니라면 
   > 관련된 모든 맵을 다시 복구시킴으로써 그 출력을 재생성해야 하기 때문이다. 따라서 프로그래머 관점으로는 태스크의 수행 순서는 중요하지 않다.

# 1.3.1 관계형 DBMS
 - 대규모 데이터 일괄 분석 상황일 경우.
 - 데이터 접근 패턴이 탐색 위주일때 커다란 데이터셋을 만들어 주고 받는 상활 일 때 탐색하고 데이터 만드는 시간이 더 오래걸리게 된다.
 - 데이터베이스의 많은 부분을 업데이트 할 때 B-Tree는 데이터베이스를 재구축하기위해 정렬/병합 을 사용하기 때문에 맵 리듀스보다 효율적이지 못함.
 - 데이터베이스는 지속적으로 업데이트되는 데이터셋에 적합. 

# 1.3.2 그리드 컴퓨팅
 - 고성능 컴퓨팅, 그리드 컴퓨팅 커뮤니티는 메시지 전달 API를 사용해 수년동안 대규모 데이터 처리를 하는 중이다.
 - HPC는 SAN 으로 연결되어 공유 파일시스템에 엑세스 하는 기계장치의 클러스터로 작업을 분산한다.
 - HPC의 SAN은 계산중심적이기 때문에 노드가 더 큰 데이터 용량을 접근하려 할 때에는 네트워크 대역폭에 병목현상이 발생하게 된다.
 - 맵리듀스는 계산 노드에 데이터를 함께 배치한다. 따라서 데이터가 로컬에 있기 때문에 데이터 접근에 빠를 수 밖에 없다.
 - '데이터 지역성' : 데이터가 로컬에 있으면 데이터 접근에 빠르다.  
 - 네트워크 대역폭은 데이터 센터 환경에서 가장 중요한 자원.
 - MPI는 프로그래머에게 사

# 1.3.3 자발적 컴퓨팅
 - SETI@Home 과 무엇이 다른가?
 - SETI@Home 은 외계 지적 생명체 신호에 대한 전파망원경 데이터를 분석하기 위해 자원자의 컴퓨터가 유휴 상태일 때 자원자가 기증한 CPU 시간 동안 SETI@home 프로젝트를 수행한다.
 - 차이점은 SETI@home은 CPU중심적이며, 전 세계 수십만개의 컴퓨터 위에서 실행되기에 적합하게 만들어 졌다.
 - 맵리듀스는 데이터센터를 통해 몇분에서 몇 시간 정도 걸리는 작업을 신뢰할 수 있는 전용 하드웨어에서 수행되도록 설계되어졌다.

# 1.4 하둡의 역사
 - 텍스트 검색 아파치 루씬 => 웹 검색 엔진 아파치 너치 => 분산 처리 파일 시스템 아파치 하둡
 - 하둡은 너치(오픈 소스 검색 엔진) 에서 시작되었다.
 - 너치의 분산처리 파트로 분리된것이 바로 하둡.
 - 야후의 도움으로 하둡은 진정으로 웹에 적용할 만한 기술로 빠르게 성장하였다.
 - 0.4 버전으로 2006년 1월경 하둡프로젝트는 아파치 서브 프로젝트로 출발함.
 - 0.4 버전에서는 두 가지 핵심 컴포넌트인 하둡 분산 파일시스템과 맵리듀스가 있었고, 이 컴포넌트들을 이용해서 논문으로만 간접 경험했던 기능과 실험들을 체험할 수 있었다.
  

# 검색엔진 만들기
 1. 크롤러(Crawler)는 웹 서버로 부터 페이지를 다운로드 하고
 2. 웹맵(WebMap)은 수집된 웹의 그래프를 구축하고
 3. 인덱서(Indexer)는 최적의 페이지로 역 색인을 구축한다. (url과 함께)
 4. 런타임(Runtime)은 사용자 쿼리를 처리한다. 
 5. 웹맵에는 웹 링크에 해당하는 대략 1조개의 연결선 edge와 고유한url에 해당하는 1000억개의 정점vertex로 구성되는 그래프 이다.
 6. 거대한 그래프를 생성하고 분석하는 것은 수많은 컴퓨터로 오랫동안 처리해야 한다.

# 1.5 아파치 하둡과 하둡 생태계
 1. 공통 : 일반적인 I/O(직렬화, 자바RPC, 영속 Persistent데이터 구조)를 위한 컴포넌트와 인터페이스 집합
 2. 에이브로 Avro : 교차언어 RPC와 영속성 데이터 스토리지를 위한 데이터 직렬화 시스템
 3. 맵리듀스 : 범용 컴퓨터의 커다란 클러스터에서 수행되는 분산 데이터 처리 모델과 실행환경
 4. HDFS 범용 컴퓨터로 된 커다란 클러스터에서 수행되는 분산 파일 시스템
 5. 피그 : 대규모 데이터셋 탐색용 데이터 흐름 언어 data flow language 와 실핼환경. HDFS와 맵 리듀스 클러스터에서 수행된다.
 6. 하이브 : 분산데이터 웨어하우스 Datawarehouse, HDFS에 저장된 데이터를 관리하고 데이터 쿼리를 위해 SQL기반 쿼리 언어를 제공한다.(런타임 엔진에 의해 맵리듀스로 변환됨)
 7. HBase : 분산 컬럼지향 데이터베이스. 스토리지로 hdfs를 사용함. 맵리듀스를 이용한 일괄 처리 방식의 계산과 랜덤 읽기가 가능한 포인트 쿼리 방식 모두 지원한다.
 8. 주키퍼 Zookeeper : 다수 컴퓨터로 분산 처리되는 고가용성 조정 서비스 . 분산 응용프로그램을 구축하기위해 사용될 수 있는 분산 락 같은 프리미티브를 제공함
 9. sqoop : 관계형 데이터베이스와 hdfs간 데이터를 효율적으로 이동시키기위한 도구
 10. 오지 : 하둡 잡(맵 리듀스, 피그, 하이브, 스쿱 잡 포함)의 워크플로우를 실행하고 스케줄링을 위한 서비스

# 과제
 1. HDFS가 이용하는 RAID방식
 2. RAID 방식의 종류
 3. ad-hoc쿼리란?
 4. B-Tree가 효율적이지 못하는 상황은 언제인가?(정렬, 병합에 비효율적인가? 정말로?)
 5. 정규표현식에 대해 배워보자
 6. 지역연산은 무엇이고 비지역 연산은 무엇인가?
 7. Scale in, Scale out 이란?
 8. HPC는 뭐하는곳이고 SAN은 무슨 뜻인가?
 9. 네트워크 위상이란?
 10. 직렬화, 자바RPC, 영속 Persistent 데이터 구조 란? 








