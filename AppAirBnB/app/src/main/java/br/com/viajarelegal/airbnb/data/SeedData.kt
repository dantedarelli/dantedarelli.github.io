package br.com.viajarelegal.airbnb.data

/**
 * Base embutida no aplicativo — nenhum banco de dados, nenhuma rede.
 *
 * Amostra estratificada (por bairro x tipo de acomodacao) extraida em 18/08/2026
 * dos arquivos ny.csv e rj.csv no padrao Inside Airbnb, mantendo apenas anuncios
 * com preco e coordenadas validos. Sorteio deterministico (semente 20260818).
 *
 * Formato: campos separados por "|", uma linha por anuncio, na ordem
 * cidade|moeda|id|nome|anfitriao|regiao|bairro|tipo|lat|lon|preco|noitesMin|
 * avaliacoes|avaliacoesMes|ultimaAvaliacao|disponibilidade365|anunciosDoAnfitriao
 */
object SeedData {

    /** Cotacao usada para trazer cada moeda a uma base comum (BRL). */
    val TAXAS_PADRAO: Map<String, Double> = mapOf(
        "USD" to 5.4,
        "BRL" to 1.0,
    )

    const val ROWS: String = """NY|USD|734810929658737801|1 Fully-Equipped Studio Apartment in The Bronx|Kyle|Bronx|Allerton|Private room|40.86477|-73.86185|79.00|30|11|0.34|2023-09-08|179|1
NY|USD|801606628280383620|New spacious renovated apartment|Bushra|Bronx|Allerton|Entire home/apt|40.86747|-73.86106|125.00|30|40|1.36|2023-12-31|365|5
NY|USD|707958114252508497|Lovely Bedroom with Jacuzzi in the host apartment|Alexandra|Queens|Arverne|Private room|40.59709|-73.79926|81.00|1|184|5.49|2025-06-01|330|1
NY|USD|7894007|MyPlace2Beach'|D.|Queens|Arverne|Entire home/apt|40.58957|-73.79093|257.00|30|92|0.77|2022-02-18|89|1
NY|USD|53373979|Lovely Curtained Sleeping Space in Astoria|Diosmery|Queens|Astoria|Private room|40.76104|-73.92158|74.00|30|67|1.56|2023-11-08|226|1
NY|USD|917633322049671093|NYC Gem. Great Location & Value.|Dragan|Queens|Astoria|Entire home/apt|40.75718|-73.92076|87.00|30|0|0.00||112|2
NY|USD|13388649|Apartment in Astoria, New York|Michelle|Queens|Astoria|Entire home/apt|40.76435|-73.90887|129.00|30|312|2.91|2025-05-31|34|1
NY|USD|33660911|Stylish Designer 3 Bedrm Apartment In Brownstone|Nadine|Queens|Astoria|Private room|40.76231|-73.91844|420.00|30|117|1.56|2023-12-05|268|1
NY|USD|52132742|Simple|Angela|Brooklyn|Bay Ridge|Private room|40.63362|-74.01823|41.00|30|1|0.10|2024-08-31|289|7
NY|USD|27712922|Bay Ridge Bklyn entire spacious Basement Apartment|Alkhadher|Brooklyn|Bay Ridge|Entire home/apt|40.63745|-74.02606|153.00|30|100|1.21|2023-07-23|320|1
NY|USD|43530040|RV living for the nature lover in you.|Zuriel|Bronx|Baychester|Entire home/apt|40.86568|-73.83403|66.00|30|0|0.00||269|1
NY|USD|50900377|Large Arty Room In Bushwick - 2|Anthony|Brooklyn|Bedford-Stuyvesant|Private room|40.68479|-73.91406|38.00|30|3|0.06|2022-05-01|320|20
NY|USD|53320872|Large HDTV Room 2 blocks to Williamsburg #268|Eugene|Brooklyn|Bedford-Stuyvesant|Private room|40.69597|-73.94568|43.00|30|0|0.00||365|592
NY|USD|1442871443313654208|The Barton – Cozy Renovated Home in Brooklyn (3C)|Briana|Brooklyn|Bedford-Stuyvesant|Private room|40.68255|-73.92221|56.00|30|0|0.00||365|11
NY|USD|54260194|Trendy & Beauty Bedroom - Desk - Brooklyn|Daniela & James|Brooklyn|Bedford-Stuyvesant|Private room|40.69975|-73.94039|57.00|30|4|0.13|2024-08-13|74|31
NY|USD|53633241|Large Room in BedStuy Duplex|Jorge|Brooklyn|Bedford-Stuyvesant|Private room|40.69437|-73.94314|58.00|30|19|0.44|2023-09-22|320|5
NY|USD|1057797641064287773|Cozy room in Bushwick Brooklyn|James|Brooklyn|Bedford-Stuyvesant|Private room|40.68351|-73.91430|75.00|30|0|0.00||269|1
NY|USD|4604991|Confortable and private room.|Luis|Brooklyn|Bedford-Stuyvesant|Private room|40.69863|-73.94397|83.00|3|395|3.22|2025-06-08|139|1
NY|USD|249867|HANCOCK VERY SMALL ROOM|Fred|Brooklyn|Bedford-Stuyvesant|Private room|40.68532|-73.91881|99.00|30|7|0.04|2018-10-07|365|4
NY|USD|950960023109020864|Palace in Brooklyn|Rose|Brooklyn|Bedford-Stuyvesant|Private room|40.69549|-73.93352|119.00|30|0|0.00||269|2
NY|USD|960901063510719919|Beautiful Bedroom in Brooklyn New- York|Robert|Brooklyn|Bedford-Stuyvesant|Private room|40.68138|-73.90983|128.00|30|0|0.00||269|1
NY|USD|40335468|Brooklyn Gem|Clive|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.69006|-73.95249|150.00|30|65|0.97|2023-10-10|269|1
NY|USD|51061698|Top Floor 1Br penthouse with large Patio ,Ac ,direct access to Amazing|Erik|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.68494|-73.92947|170.00|30|3|0.07|2023-05-07|270|1
NY|USD|1016825148595065353|HOME away from Home with fast Wi-Fi, yard & office|Ran Dan|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.68121|-73.91828|171.00|30|2|0.20|2025-01-07|306|1
NY|USD|53294328|Historic 1 bedroom rental in Bed Stuy Brownstone|Gabrielle|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.68379|-73.94738|200.00|30|24|0.60|2025-05-01|109|1
NY|USD|993563148062414056|Cozy & Modern Brooklyn Retreat|Devora|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.67864|-73.94448|268.00|1|45|2.24|2025-05-02|38|9
NY|USD|1617488|Bohemian Brooklyn Loft with Private Terrace!|Stephanie|Brooklyn|Bedford-Stuyvesant|Entire home/apt|40.69155|-73.95742|293.00|30|84|0.59|2023-11-27|70|1
NY|USD|48109162|“Urban Crib” modern 1BR APT close to JFK/LGA|Nabil|Queens|Bellerose|Entire home/apt|40.73312|-73.73666|105.00|30|125|2.37|2025-05-15|226|1
NY|USD|985326372803136953|A Share room at Brooklyn|Bin|Brooklyn|Bensonhurst|Private room|40.61388|-74.00129|195.00|2|5|0.57|2025-05-18|237|1
NY|USD|629196150330969487|Lovely room in Brooklyn|Wismick|Brooklyn|Bergen Beach|Private room|40.62528|-73.91140|150.00|30|29|0.82|2023-09-05|365|1
NY|USD|23149791|Cozy room.|Valentina|Brooklyn|Borough Park|Private room|40.64400|-73.99637|55.00|30|15|0.18|2024-10-06|213|2
NY|USD|629802358927619607|Cozy clean big studio apartment close to subway|Samanta|Brooklyn|Borough Park|Entire home/apt|40.62892|-73.99328|89.00|30|5|0.14|2023-04-30|309|1
NY|USD|716577023762617548|Spacious Studio Room with Private Bathroom|Ajmol|Queens|Briarwood|Private room|40.70973|-73.80849|50.00|30|2|0.06|2023-01-15|111|4
NY|USD|884977596472621491|A Cozy Home Away From Home|Kierra|Queens|Briarwood|Entire home/apt|40.71273|-73.81351|200.00|30|4|0.16|2023-09-03|365|1
NY|USD|47968434|Brighton Beach Studio walk to ocean, rent Monthly|Mikhail|Brooklyn|Brighton Beach|Entire home/apt|40.57884|-73.96573|59.00|30|13|0.25|2024-01-05|353|4
NY|USD|11371535|Cozy and bright bedroom with a queen size bed.|Donald|Bronx|Bronxdale|Private room|40.85306|-73.86644|34.00|30|118|1.05|2025-06-04|43|1
NY|USD|975990|Brooklyn Heights Brownstone - Private Bdrm Avail.|Gerard|Brooklyn|Brooklyn Heights|Entire home/apt|40.69470|-73.99525|300.00|30|13|0.10|2019-11-29|363|1
NY|USD|9351006|Cozy & Comfortable Private Room / Private Bathroom|Yemi|Brooklyn|Brownsville|Private room|40.66530|-73.91737|63.00|30|172|1.49|2024-10-08|108|3
NY|USD|1354771911555593980|sleep well|Joyhefsiba|Brooklyn|Bushwick|Shared room|40.69800|-73.92731|43.00|30|0|0.00||365|6
NY|USD|6659165|Room available in BUSHWICK!|Julie|Brooklyn|Bushwick|Private room|40.70291|-73.92887|67.00|30|41|0.34|2024-10-05|287|3
NY|USD|33725862|Room in 3BR1BA, 9mins walk > J line Halsey Station|Kaz|Brooklyn|Bushwick|Private room|40.69038|-73.91228|68.00|30|20|0.30|2025-03-04|292|82
NY|USD|1424485625198090530|Quiet & Cozy Bushwick Room|Maria|Brooklyn|Bushwick|Private room|40.69088|-73.91454|81.00|30|0|0.00||45|1
NY|USD|660082095537116529|✰Spacious, bright & modern room in Prime Bushwick!|Philip|Brooklyn|Bushwick|Private room|40.69436|-73.92884|98.00|3|131|3.65|2025-06-05|278|2
NY|USD|1343244748884303826|One Bedroom for Longer Stays|Yotlaire|Brooklyn|Bushwick|Entire home/apt|40.69563|-73.93098|115.00|30|1|0.37|2025-03-28|321|7
NY|USD|734682208263244829|Unique and Authentic Qazaq Nomadic Stay|Ǧani|Brooklyn|Bushwick|Entire home/apt|40.68147|-73.90546|200.00|30|11|0.35|2023-08-22|365|4
NY|USD|1408269924176237336|modern oasis 1|Dianne|Queens|Cambria Heights|Entire home/apt|40.70258|-73.73110|152.00|30|0|0.00||363|3
NY|USD|39587759|Rhonda’s Place|Rhonda|Brooklyn|Canarsie|Private room|40.63226|-73.90870|93.00|30|1|0.02|2020-01-01|263|1
NY|USD|13674799|Private suite in Carroll Gardens|Mark|Brooklyn|Carroll Gardens|Private room|40.67796|-73.99871|130.00|2|568|5.39|2025-06-13|29|1
NY|USD|1061461150709867848|Private Basement w/ Wash & Park!|Anika|Staten Island|Castleton Corners|Private room|40.61275|-74.11223|45.00|30|0|0.00||167|1
NY|USD|979755218088178203|Stylish Chelsea studio - Perfect location|Josh|Manhattan|Chelsea|Entire home/apt|40.74544|-73.99614|135.00|30|5|0.25|2025-05-15|282|1
NY|USD|30046573|Cheery Chelsea Charmer, flexible dates and rates!|Erica|Manhattan|Chelsea|Entire home/apt|40.74296|-73.99838|187.00|30|191|2.46|2024-10-16|75|1
NY|USD|1173119599443925057|Blueground / Chelsea, Laundry & Elevator|Blueground|Manhattan|Chelsea|Entire home/apt|40.74480|-74.00536|397.00|31|0|0.00||365|1054
NY|USD|615126076185455171|Charming Pre-War 1 Bd West Village Designer Apt|Natalie|Manhattan|Chelsea|Entire home/apt|40.74101|-74.00441|400.00|30|15|0.43|2023-08-09|355|1
NY|USD|53349734|Blueground / Chelsea, gym & doorman, nr Penn Stn|Blueground|Manhattan|Chelsea|Entire home/apt|40.75301|-73.99593|557.00|31|0|0.00||60|1054
NY|USD|1224079069637556316|In The Heart Of Lower East Side / Accessible Room|Crishele Mae|Manhattan|Chinatown|Private room|40.71721|-73.99553|228.00|1|5|0.63|2025-05-28|280|9
NY|USD|9147025|Cozy City Island Cottage|Diane|Bronx|City Island|Entire home/apt|40.84637|-73.78857|111.00|30|187|1.62|2024-08-22|290|1
NY|USD|23784779|4 Queen Beds - Downtown Manhattan Entire Apartment|Jayu|Manhattan|Civic Center|Entire home/apt|40.71321|-73.99935|293.00|30|236|2.68|2025-05-23|170|1
NY|USD|63588|LL3|Carol Gloria|Bronx|Clason Point|Private room|40.81161|-73.85499|91.00|30|0|0.00||0|6
NY|USD|897622871165711966|Cozy 1 Bdrm #2 in 3 Bdrm Loft|Isamar|Brooklyn|Clinton Hill|Private room|40.69228|-73.96156|135.00|30|4|0.18|2023-09-22|180|2
NY|USD|899892308740180470|Bohemian Living NYC|Marie Louise|Brooklyn|Clinton Hill|Entire home/apt|40.68354|-73.96595|296.00|30|0|0.00||269|2
NY|USD|812824433682429889|Blueground / Clinton Hill, gym, pool & w/d|Blueground|Brooklyn|Clinton Hill|Entire home/apt|40.68201|-73.96748|475.00|31|0|0.00||321|1054
NY|USD|1268379012796078651|Waterfront 1-Bedroom AC-WiFi|Franklin|Queens|College Point|Private room|40.79087|-73.83896|132.00|30|0|0.00||365|2
NY|USD|53299304|The Little Paradise - New Owner|Nidal|Staten Island|Concord|Entire home/apt|40.60531|-74.08270|299.00|30|8|0.19|2023-11-05|365|1
NY|USD|1385758419382486826|Cozy Bedroom in Historic Townhouse|Edwin|Bronx|Concourse|Private room|40.81930|-73.92742|79.00|30|0|0.00||365|1
NY|USD|9312190|Gorgeous 2Bd,1Ba; 15 min to midtown; sleeps 5|Diana|Bronx|Concourse|Entire home/apt|40.81977|-73.92752|108.00|30|0|0.00||153|2
NY|USD|51150521|Let’s stay home(1 bottle complementary wine )|Raymond|Bronx|Concourse Village|Private room|40.83575|-73.91527|94.00|30|33|0.70|2023-10-16|365|2
NY|USD|778457317754893437|Artist's Sun-filled Home|Natalie|Bronx|Concourse Village|Entire home/apt|40.82732|-73.91947|161.00|30|33|1.14|2025-05-28|63|16
NY|USD|837888270806369904|Queen Bed, Cozy, 50' TV, Low Cleaning Fee!|Norbert|Brooklyn|Coney Island|Private room|40.57735|-73.98282|90.00|30|20|0.73|2024-07-14|289|11
NY|USD|1113463250186485659|Bohemian Beach House Bungalow|Kara|Brooklyn|Coney Island|Entire home/apt|40.57417|-73.98933|94.00|30|0|0.00||269|1
NY|USD|943780020325684092|Comfy / “Couch surfers” dream / near LGA &JFK|Veronica|Queens|Corona|Shared room|40.74823|-73.86317|63.00|1|33|1.50|2025-06-02|358|3
NY|USD|1290712572437787645|Beautiful Suite near LGA airport & Citi field|Jason|Queens|Corona|Private room|40.74204|-73.86668|95.00|2|8|4.29|2025-06-15|75|1
NY|USD|577671424155519066|Comfy spacious Queen bed|Oceanhill|Brooklyn|Crown Heights|Private room|40.67321|-73.92021|75.00|30|26|0.67|2022-10-16|363|2
NY|USD|49424846|Cozy Exposed Brick Gem~BK|Lourdie|Brooklyn|Crown Heights|Private room|40.67140|-73.93228|90.00|30|1|0.02|2021-08-28|270|1
NY|USD|45623054|Crown Heights Chic, Spacious & Centrally Located!|Wanda|Brooklyn|Crown Heights|Entire home/apt|40.67177|-73.94899|210.00|3|169|4.00|2025-06-13|213|1
NY|USD|921500|Beautiful Brooklyn Brownstone|Anna & Mike|Brooklyn|Crown Heights|Entire home/apt|40.67075|-73.95179|250.00|30|2|0.01|2025-05-30|9|1
NY|USD|585116610819537455|Stylish Apartment with Patio in Brooklyn|Kieu|Brooklyn|Crown Heights|Entire home/apt|40.67655|-73.96235|425.00|30|25|0.69|2023-11-25|89|5
NY|USD|820973617127079733|1st floor :Room 3|Umme Salma|Brooklyn|Cypress Hills|Private room|40.67974|-73.86547|44.00|30|5|0.19|2024-08-08|325|7
NY|USD|968138190116958758|Lux Oasis in the Heart of Bklyn|Gabriel|Brooklyn|Cypress Hills|Entire home/apt|40.67797|-73.89480|250.00|30|1|0.05|2023-09-14|286|3
NY|USD|83474|NY Mag & Budget Travel love us!|Matthew|Brooklyn|Downtown Brooklyn|Private room|40.69132|-73.98573|166.00|30|5|0.03|2014-12-18|335|1
NY|USD|1303498152597541153|Blueground / Downtown BK, gym & w/d, nr BAM|Blueground|Brooklyn|Downtown Brooklyn|Entire home/apt|40.68889|-73.98125|329.00|31|0|0.00||210|1054
NY|USD|24996332|Master Bedroom with Full Bath & Manhattan View|Win|Queens|East Elmhurst|Private room|40.75800|-73.86991|87.00|1|595|6.90|2025-06-05|152|2
NY|USD|35172393|'For business or pleasure'|Leisha|Brooklyn|East Flatbush|Private room|40.65573|-73.94012|80.00|30|1|0.01|2019-09-02|365|3
NY|USD|636216578068395468|Artsy, large, luxurious garden open plan apartment|Ian|Brooklyn|East Flatbush|Entire home/apt|40.64692|-73.94551|121.00|30|11|0.31|2025-05-17|177|1
NY|USD|1037893954700010142|Brooklyn's Heart|Levi|Brooklyn|East Flatbush|Entire home/apt|40.65542|-73.92210|174.00|1|103|5.70|2025-06-16|262|3
NY|USD|36152646|Peace of Brooklyn|Lawanne|Brooklyn|East Flatbush|Private room|40.64573|-73.94859|190.00|3|7|0.10|2025-01-05|171|2
NY|USD|1384026572935250915|Apt Near Central Park & 6 Train|Omar|Manhattan|East Harlem|Entire home/apt|40.79308|-73.94037|84.00|31|0|0.00||320|2
NY|USD|19912695|Is your moment!! Take it!!|Alan|Manhattan|East Harlem|Private room|40.79137|-73.94388|85.00|30|77|1.65|2025-05-31|266|1
NY|USD|1056376520439117729|Bright and lovely room in NYC|Santiago|Manhattan|East Harlem|Private room|40.79045|-73.93926|86.00|30|4|0.32|2024-11-30|131|4
NY|USD|60611|SpaHa Studio Monthly Rental|Blanca|Manhattan|East Harlem|Entire home/apt|40.79267|-73.94653|125.00|30|196|1.14|2023-10-12|227|4
NY|USD|1093627832142540212|Spacious 1BR w/ Garden UES Prime Central Park|Frotas|Manhattan|East Harlem|Entire home/apt|40.78849|-73.94715|130.00|37|7|0.47|2025-05-16|217|8
NY|USD|32607488|2nd Floor, Room # 2 (12'x14')|Aminul|Brooklyn|East New York|Private room|40.66762|-73.89573|56.00|30|18|0.26|2025-01-09|264|9
NY|USD|910771154080515905|Modern Studio Apartment near JFK|Rafael|Brooklyn|East New York|Entire home/apt|40.66765|-73.85731|208.00|30|15|0.61|2023-10-08|87|2
NY|USD|989273077282515810|1BR Artsy Attic near Trains in Brooklyn|Brooklyn|Brooklyn|East New York|Entire home/apt|40.67612|-73.88509|215.00|2|15|0.74|2025-05-24|80|4
NY|USD|659310625338344733|Large Bedrooom in east village apartment|Alex|Manhattan|East Village|Private room|40.72860|-73.98664|141.00|1|13|0.37|2024-10-20|342|3
NY|USD|16580|Sunny, Modern room in East Village w City Views|Reka|Manhattan|East Village|Private room|40.72373|-73.97626|198.00|1|642|4.46|2025-06-01|208|2
NY|USD|1176163500569034058|2A-Comfortable King Modern Room|Nelson|Bronx|Edenwald|Private room|40.89104|-73.83999|42.00|30|5|0.45|2025-06-14|56|2
NY|USD|53658866|The Baychester Studio|George|Bronx|Edenwald|Entire home/apt|40.89268|-73.83935|112.00|30|22|0.52|2024-07-01|346|1
NY|USD|763886203005710009|Room in 3BR2BA Apt near Jackson Hts–Roosevelt Ave|Kaz|Queens|Elmhurst|Private room|40.74616|-73.88876|75.00|30|6|0.20|2025-06-06|332|82
NY|USD|16965094|2 Person Private Room * Hungry for a Kitchen ?|Carlos|Queens|Elmhurst|Private room|40.74554|-73.88280|126.00|5|205|2.04|2025-06-14|336|5
NY|USD|898047985441174541|Cozy home in the heart of Middle Village, Queens.|Kevin|Queens|Elmhurst|Entire home/apt|40.72805|-73.88026|155.00|30|3|0.12|2023-07-23|365|1
NY|USD|1032905729887293427|Beautiful Studio Downtown NYC|Vincenco|Manhattan|Financial District|Entire home/apt|40.70734|-74.00549|302.00|3|37|2.35|2025-05-30|105|3
NY|USD|1405264326081758314|King Room - 1 bed|Suiteness|Manhattan|Financial District|Private room|40.70528|-74.00751|569.00|1|0|0.00||365|19
NY|USD|1353701430247771450|Comfortable Private Room #1573 D|Tina|Brooklyn|Flatbush|Private room|40.65468|-73.95643|54.00|30|0|0.00||119|280
NY|USD|48782140|Very clean , cozy , warm and spacious apartment .|Razack|Brooklyn|Flatbush|Entire home/apt|40.65442|-73.95982|81.00|32|13|0.26|2025-05-21|151|1
NY|USD|880643090819651668|1 out of a million|Welcome To Excellent Homes|Brooklyn|Flatbush|Entire home/apt|40.65473|-73.95566|220.00|30|5|0.20|2023-06-18|365|10
NY|USD|613747202944317309|Sonder Henri on 24 / Accessible King Room|Sonder NYC|Manhattan|Flatiron District|Private room|40.74380|-73.98994|311.00|1|47|1.24|2025-05-14|0|87
NY|USD|743552965365692523|A cozy room away from home|Jacintha|Brooklyn|Flatlands|Private room|40.62557|-73.94348|57.00|30|19|0.62|2023-11-05|192|2
NY|USD|821745411929728188|The 3BR Delux-NearJFK, Beaches & Brooklyn College|Mercedes|Brooklyn|Flatlands|Entire home/apt|40.62799|-73.93915|220.00|30|0|0.00||364|1
NY|USD|30537134|202#Flushing downtown luxury suite|Cindy|Queens|Flushing|Private room|40.74392|-73.83404|96.00|1|212|2.67|2025-06-15|173|7
NY|USD|1135404412261240153|Prívate room for 2 in a quiet area in NY|Priscila|Queens|Flushing|Private room|40.76083|-73.80438|167.00|2|22|1.56|2025-06-09|361|2
NY|USD|35409062|Next to Train B/D/4 Hospital & Campus|Dean|Bronx|Fordham|Private room|40.86091|-73.90002|88.00|30|6|0.09|2021-01-25|269|1
NY|USD|1433605916811085516|Heart of Forest Hills|Tolga|Queens|Forest Hills|Entire home/apt|40.72036|-73.84267|179.00|30|0|0.00||44|2
NY|USD|54167112|Lovely studio apartment steps from Ft Greene Park|Ian|Brooklyn|Fort Greene|Entire home/apt|40.69006|-73.97348|155.00|30|19|0.47|2025-05-20|235|1
NY|USD|7951281|Beautiful 2BR heart of Fort Greene|Jonathan|Brooklyn|Fort Greene|Entire home/apt|40.68899|-73.97269|315.00|30|319|2.70|2023-10-26|365|2
NY|USD|1002823881758639223|Welcome home my lovely guest|Julia|Brooklyn|Fort Hamilton|Private room|40.61460|-74.03510|103.00|30|0|0.00||269|2
NY|USD|1343440696309404963|2BR: Spacious, Stylish, Ideal for Groups|Shlome|Brooklyn|Fort Hamilton|Entire home/apt|40.61881|-74.03238|198.00|1|6|1.80|2025-05-26|206|64
NY|USD|1380134373284027763|Tiny bedroom AC / 1 Person Only + Free Parking|Peipei|Queens|Fresh Meadows|Private room|40.73900|-73.78703|53.00|1|6|3.83|2025-05-27|137|5
NY|USD|823187166558406333|Peaceful & Cozy Private Full Room / Bushwick|Auset|Queens|Glendale|Private room|40.69790|-73.89355|67.00|30|8|0.32|2024-02-01|94|3
NY|USD|47887785|Private Studio in Glendale, Queens / Free W/D|Bruce|Queens|Glendale|Entire home/apt|40.70542|-73.87492|196.00|30|109|2.04|2024-10-01|365|10
NY|USD|1178925366985309887|Manhattan views & free brekkie in hip Boerum Hill|Baltic Hotel|Brooklyn|Gowanus|Private room|40.68262|-73.98553|160.00|1|33|2.81|2025-04-27|3|1
NY|USD|8873479|Beautiful modern room with private bathroom|Darin|Manhattan|Gramercy|Private room|40.73561|-73.98183|131.00|1|299|2.57|2025-06-06|19|1
NY|USD|818125827182233766|Great Little Retro Getaway newly renovated|Lorenzina Antonella|Brooklyn|Gravesend|Entire home/apt|40.60531|-73.97491|130.00|30|10|0.47|2025-04-07|192|1
NY|USD|41420151|Noble House #3|Jordan|Brooklyn|Greenpoint|Private room|40.72753|-73.95606|65.00|30|35|0.54|2025-06-12|166|3
NY|USD|18838040|Historic District: Enchanting, quiet & peaceful|Michael|Brooklyn|Greenpoint|Entire home/apt|40.72904|-73.95469|299.00|1|51|0.54|2025-05-18|26|1
NY|USD|40350292|Superior Studio Loft In Greenpoint|Henry|Brooklyn|Greenpoint|Entire home/apt|40.72883|-73.94485|348.00|3|6|0.09|2022-10-31|48|14
NY|USD|1049940767703198807|Bright, Spacious 2BR w Park view|Albert|Brooklyn|Greenpoint|Entire home/apt|40.71993|-73.95476|429.00|30|0|0.00||324|1
NY|USD|21010586|Cozy Junior One Bedroom apt in Greenwich Village|Nicola|Manhattan|Greenwich Village|Entire home/apt|40.73275|-74.00005|179.00|30|37|0.40|2025-05-08|255|5
NY|USD|1010200773712690524|Lovely ROOM|Kristina|Manhattan|Harlem|Private room|40.81586|-73.94441|43.00|30|0|0.00||269|16
NY|USD|42586407|Comfortable Private Room — Short Stay? Just Ask!|Michel|Manhattan|Harlem|Private room|40.82545|-73.95316|46.00|30|24|0.94|2025-04-03|98|1
NY|USD|1204424191855903551|Amazing Bedroom Upper Manhattan|Kristina|Manhattan|Harlem|Private room|40.80996|-73.94448|52.00|30|0|0.00||352|16
NY|USD|49963985|MASIVE ROOM-WASHER DRYER-RENT BY THE CALENDAR MONT|Ozgur|Manhattan|Harlem|Private room|40.82238|-73.94669|65.00|30|4|0.10|2024-06-30|167|6
NY|USD|18226214|Hudson River Oasis Suite|Gulden|Manhattan|Harlem|Private room|40.82247|-73.95397|132.00|2|34|0.35|2025-05-29|325|2
NY|USD|1364973167063797281|Uptown full 1 bedroom in Harlem|Quamaine|Manhattan|Harlem|Entire home/apt|40.82053|-73.94571|182.00|31|0|0.00||365|1
NY|USD|19297780|Beautiful, Sunny, Spacious, & Serene 3-Bedroom|Heather|Manhattan|Harlem|Entire home/apt|40.82444|-73.94375|184.00|90|0|0.00||269|2
NY|USD|1150083229376322051|Beautiful 3BR Uptown Townhouse|Andrew|Manhattan|Harlem|Entire home/apt|40.82874|-73.94818|268.00|30|0|0.00||79|1
NY|USD|44035515|Beautiful oasis in the heart of NYC|Batia|Manhattan|Harlem|Entire home/apt|40.83059|-73.94173|278.00|30|2|0.06|2024-09-06|3|1
NY|USD|51670813|Lovely Room / PRIVATE BATHROOM|Kristina|Manhattan|Hell's Kitchen|Private room|40.76189|-73.99152|99.00|30|0|0.00||304|12
NY|USD|1224933353394487480|Chic Room in Timesquare NYC|Hady|Manhattan|Hell's Kitchen|Private room|40.75889|-73.99210|100.00|30|0|0.00||359|5
NY|USD|1394250288752503514|Classic Private Room #144 B|Tina|Manhattan|Hell's Kitchen|Private room|40.75542|-73.99360|108.00|30|0|0.00||236|280
NY|USD|36803276|Cozy apartment near Columbus Circle/Central Park|Amedeo|Manhattan|Hell's Kitchen|Entire home/apt|40.76517|-73.98424|123.00|30|3|0.06|2024-05-18|133|5
NY|USD|1308810839695104610|10 Minute Walk to Times Square|Allyza|Manhattan|Hell's Kitchen|Entire home/apt|40.75998|-73.98824|303.00|3|5|1.33|2025-05-23|253|3
NY|USD|47333781|2BR In Times Square Near Restaurants|Shannon|Manhattan|Hell's Kitchen|Entire home/apt|40.76125|-73.99189|382.00|2|202|3.76|2025-06-06|185|10
NY|USD|1437558502175065750|Blueground / Hell's Kitchen, BBQ, nr Hudson River|Blueground|Manhattan|Hell's Kitchen|Entire home/apt|40.76170|-73.99962|649.00|30|0|0.00||306|1054
NY|USD|1042054162835980391|Hudson Suite at Ink 48 Hotel, Close to Pier 84!|RoomPicks|Manhattan|Hell's Kitchen|Hotel room|40.76458|-73.99595|50046.00|1|0|0.00||352|27
NY|USD|1110284715112501472|Cozy share 1Bedroom Apartment|Martha|Bronx|Highbridge|Private room|40.84120|-73.92599|81.00|30|0|0.00||364|1
NY|USD|874094083624913235|Comfy apartment|Ramon|Queens|Hollis|Entire home/apt|40.71206|-73.76785|95.00|30|3|0.12|2024-06-28|89|3
NY|USD|1382542169787026641|we got a bed come lay your head|Shantel Britney|Queens|Howard Beach|Private room|40.65272|-73.82892|60.00|30|0|0.00||89|1
NY|USD|856735434937708938|Perfect location in Howard Beach|Samantha|Queens|Howard Beach|Entire home/apt|40.65664|-73.84526|127.00|30|1|0.05|2023-08-27|364|1
NY|USD|54214433|Manhattan, área de fácil transporte|Adriel|Manhattan|Inwood|Private room|40.86349|-73.92735|52.00|30|18|0.43|2022-04-26|213|1
NY|USD|1339181854607087478|Newly furnished one bedroom apartment|Zaire|Manhattan|Inwood|Entire home/apt|40.86809|-73.92447|129.00|30|2|0.65|2025-05-18|358|2
NY|USD|15845215|Cozy bedroom apt near to LGA airport. Free Parking|Tashi|Queens|Jackson Heights|Entire home/apt|40.75106|-73.89411|89.00|30|204|1.95|2020-02-28|321|1
NY|USD|965505333713118531|Home-feel New Bedroom|Vincent|Queens|Jackson Heights|Private room|40.74779|-73.88817|97.00|3|45|2.07|2025-06-14|169|3
NY|USD|36236515|Tranquil Haven-8 mins to JFK- Rm.#3|Sharon|Queens|Jamaica|Private room|40.67245|-73.78054|74.00|1|405|5.64|2025-06-15|159|3
NY|USD|787639567025687909|Elegant 4 Br NearJFK/LGA Airports & Resorts Casino|Ima|Queens|Jamaica|Entire home/apt|40.70212|-73.81056|224.00|30|0|0.00||346|1
NY|USD|611470894386067928|XL Luxe 3BR/2BTH JFK5min/USB 10min Private Parking|KerryAnne|Queens|Jamaica|Entire home/apt|40.67961|-73.78143|412.00|2|100|2.64|2025-06-09|331|1
NY|USD|1110997503201864450|Prime Escape – 15 Mins to JFK/LGA & Near Manhattan|Mohammad|Queens|Jamaica Hills|Entire home/apt|40.71400|-73.79419|519.00|2|3|0.55|2025-04-21|347|1
NY|USD|1338629253127116048|Newly Renovated Master Bedroom|Chunpeng|Brooklyn|Kensington|Private room|40.64307|-73.98180|53.00|30|0|0.00||137|2
NY|USD|631700434097171960|Private room with comfortable bed and mattress|Gregory,Avner|Queens|Kew Gardens|Private room|40.71022|-73.82873|55.00|30|3|0.08|2022-06-21|319|17
NY|USD|764204323992384659|Lovely room in NYC|Yanela|Bronx|Kingsbridge|Private room|40.88280|-73.89533|71.00|30|4|0.16|2024-10-15|0|2
NY|USD|736937979271475200|Lovely 1Br near Fordham University|Ashanty|Bronx|Kingsbridge|Entire home/apt|40.87361|-73.90237|108.00|30|3|0.10|2024-08-31|289|1
NY|USD|22020696|3 A LARGE & GREAT STUDIO APT GREAT LOCATION NYC|Julia|Manhattan|Kips Bay|Entire home/apt|40.74171|-73.98224|111.00|30|0|0.00||313|6
NY|USD|862911460310554685|Spacious room with private bathroom &2 double beds|Aamer|Manhattan|Kips Bay|Private room|40.74364|-73.98132|114.00|30|0|0.00||130|25
NY|USD|43909885|24-5 Furnished Studio W/D Prime Gramercy|Urban Furnished|Manhattan|Kips Bay|Entire home/apt|40.73891|-73.98083|215.00|30|8|0.16|2024-05-08|318|237
NY|USD|288726|Cozy Railroad Apt.|Flo|Manhattan|Little Italy|Private room|40.71900|-73.99764|87.00|30|59|0.38|2025-05-09|55|1
NY|USD|43310098|138 Bowery-Classic Queen Studio|Jeniffer|Manhattan|Little Italy|Entire home/apt|40.71887|-73.99584|213.00|15|61|1.00|2025-05-30|78|47
NY|USD|806386854940901670|Queens HDTV Room, AC, 13 mins to Manhattan #451|Eugene|Queens|Long Island City|Private room|40.75574|-73.93556|36.00|30|0|0.00||365|592
NY|USD|805345391228904561|Queens Room, Private Bath, 13min to Manhattan #449|Eugene|Queens|Long Island City|Private room|40.75552|-73.93506|46.00|30|0|0.00||365|592
NY|USD|52459126|Luxury Wellness Space - Breathtaking NYC Views|Sneha|Queens|Long Island City|Entire home/apt|40.75374|-73.92452|135.00|30|9|0.21|2024-12-23|251|3
NY|USD|9899132|Spacious Private 1-Bedroom Suite in LIC|James|Queens|Long Island City|Entire home/apt|40.74514|-73.95295|167.00|30|212|1.84|2025-04-05|79|1
NY|USD|821008630461119661|69-5A Celebrate the Season: Modern Lower East Side|Urban Furnished|Manhattan|Lower East Side|Entire home/apt|40.71936|-73.98511|196.00|30|2|0.19|2024-11-17|342|237
NY|USD|266753|2 Bedroom apt.in Manhattan/New York|Sinem|Manhattan|Lower East Side|Entire home/apt|40.71941|-73.98968|235.00|30|90|0.57|2023-02-18|365|3
NY|USD|31533518|Clean & Modern 1-BR Apt in the heart of Manhattan|Ghazi|Manhattan|Lower East Side|Entire home/apt|40.71170|-73.99033|250.00|30|1|0.01|2019-03-10|83|1
NY|USD|1146753963938449419|Chic Urban Unit w/ Rooftop Bar + Wellness Center|RoomPicks|Manhattan|Lower East Side|Hotel room|40.72311|-73.99209|386.00|1|3|0.24|2025-02-17|257|157
NY|USD|41634451|Beautiful one bedroom basement apartment|Cristina|Staten Island|Mariners Harbor|Entire home/apt|40.63686|-74.15370|73.00|30|19|0.29|2025-05-16|134|1
NY|USD|1127744374249883129|Quaint Home Near All|Robert|Staten Island|Mariners Harbor|Private room|40.63191|-74.16529|133.00|30|0|0.00||365|1
NY|USD|871690724659514256|Dormitorio, grande y cómodo.|Maria|Queens|Maspeth|Private room|40.71820|-73.90334|57.00|30|54|2.08|2025-06-13|364|2
NY|USD|31317744|Seretse's Inn will be shared with the owner.|Seretse'S|Bronx|Melrose|Private room|40.82450|-73.91501|176.00|5|80|1.03|2025-01-02|365|1
NY|USD|53469|cozy studio with parking spot|Mark|Queens|Middle Village|Entire home/apt|40.71567|-73.87842|117.00|30|33|0.18|2015-05-09|365|10
NY|USD|1366232581216853556|Manhattan Midtown East Gem|Angelica|Manhattan|Midtown|Entire home/apt|40.75456|-73.96857|129.00|30|0|0.00||83|1
NY|USD|953728610532190232|15 ft ceiling Bright Studio|Wapt|Manhattan|Midtown|Entire home/apt|40.74925|-73.98272|133.00|30|1|0.10|2024-08-31|230|32
NY|USD|53590193|Exclusive Apartment 217 / Shared Bathroom|Eliza|Manhattan|Midtown|Private room|40.74772|-73.98893|141.00|1|34|0.89|2025-02-25|241|69
NY|USD|53420671|Lovely 1-bedroom Open Concept Condo in New York C|Labeat|Manhattan|Midtown|Entire home/apt|40.75476|-73.96611|170.00|30|55|1.42|2025-05-24|270|1
NY|USD|1087588544639499882|Double bed in Turtle Bay|Juan|Manhattan|Midtown|Entire home/apt|40.75558|-73.97285|240.00|30|3|0.36|2025-05-03|37|46
NY|USD|1092928294708580275|Big Apple Experience / Central Park. Gym|Hilton Garden Inn Central Park S|Manhattan|Midtown|Private room|40.76482|-73.98416|280.00|1|236|15.95|2025-06-02|155|5
NY|USD|51356492|Cozy Family Charm|Francisco|Manhattan|Midtown|Entire home/apt|40.74690|-73.98666|280.00|4|126|2.75|2025-06-09|310|1
NY|USD|1106258701477536746|Prime Location/Comfy & Inviting|Boomerang|Manhattan|Midtown|Entire home/apt|40.75716|-73.98154|320.00|30|0|0.00||291|26
NY|USD|1101479604660196862|The East Star|Matt|Manhattan|Midtown|Entire home/apt|40.75494|-73.96431|349.00|30|1|0.16|2024-12-12|256|17
NY|USD|904537540621885048|1-Bedroom Hotel Suite - 2 beds|Suiteness|Manhattan|Midtown|Private room|40.75592|-73.98214|354.00|1|0|0.00||119|45
NY|USD|656289993889198021|Blueground / Midtown E, elevator, nr shopping|Blueground|Manhattan|Midtown|Entire home/apt|40.75478|-73.96689|395.00|31|0|0.00||333|1054
NY|USD|1023812249195453762|Spend New Year's Eve in Midtown NYC! 12/30-1/3/26|David|Manhattan|Midtown|Private room|40.75266|-73.97248|475.00|4|0|0.00||19|1
NY|USD|1164463658104314696|Deluxe Double Room Near NAYA NYC|LuxurybookingsFZE|Manhattan|Midtown|Entire home/apt|40.76015|-73.97388|822.00|30|0|0.00||363|330
NY|USD|25141552|Private cozy room in Ditmas Park best loc|Rivka|Brooklyn|Midwood|Private room|40.63050|-73.96639|58.00|30|34|0.39|2020-09-29|365|2
NY|USD|1130352927661347785|Room with private bathroom (Room nr. 2)|Shimon|Brooklyn|Mill Basin|Private room|40.60707|-73.91534|247.00|2|5|0.78|2025-06-08|268|4
NY|USD|39189765|Columbia University Park View Studio 2|Georgia|Manhattan|Morningside Heights|Entire home/apt|40.80371|-73.95930|125.00|30|2|0.14|2024-07-14|169|7
NY|USD|11069406|Artsy Private Room w-Firescape|Erick|Manhattan|Morningside Heights|Private room|40.80284|-73.95931|191.00|31|47|1.57|2023-10-10|270|2
NY|USD|38879859|Montefiore Profess. 5 min walk/long term discount|Michael|Bronx|Morris Park|Private room|40.84622|-73.84862|70.00|30|66|0.98|2024-10-08|318|2
NY|USD|754330591139720659|2 Bdr Washer/Dryer in-Unit, King bed, Entire unit|John|Bronx|Morris Park|Entire home/apt|40.85183|-73.86150|120.00|30|9|0.41|2025-05-20|62|2
NY|USD|735740047616638963|Charming suite in historic townhouse|Thomas|Bronx|Mott Haven|Private room|40.80811|-73.92104|159.00|1|36|1.40|2025-05-17|267|1
NY|USD|46729678|Perfect for summer interns midtown Manhattan|Emmory|Manhattan|Murray Hill|Entire home/apt|40.74676|-73.97624|214.00|30|73|1.35|2024-11-20|311|6
NY|USD|21073975|Charming 1 bedroom in mid-town Manhattan|Stanley|Manhattan|Murray Hill|Hotel room|40.74861|-73.97793|240.00|30|2|0.02|2018-07-28|364|83
NY|USD|1104924591396866069|Blueground / Murray Hill, gym, nr Grand Central|Blueground|Manhattan|Murray Hill|Entire home/apt|40.74833|-73.98094|531.00|31|0|0.00||168|1054
NY|USD|702012279693056539|The Tuscany Powered by LuxUrban Tri, 1 King,1 Twin|LuxUrban|Manhattan|Murray Hill|Private room|40.74940|-73.97921|1027.00|30|1|0.03|2022-12-08|365|32
NY|USD|41547677|Cozy Home with separate office!! Or 2nd bedroom!!|Jon|Manhattan|NoHo|Entire home/apt|40.72530|-73.99395|295.00|30|10|0.18|2022-10-31|320|2
NY|USD|896939245238848955|Early Modern Period home in NY Upscale Riverdale|Sergio|Bronx|North Riverdale|Private room|40.90320|-73.89801|175.00|30|3|0.13|2024-05-21|309|1
NY|USD|1293887002113007059|Chic Bronx Home By Montefiore Hospital 4BDS 2BTHS|Guy|Bronx|Norwood|Entire home/apt|40.87659|-73.87889|176.00|30|0|0.00||345|3
NY|USD|1092927557165898276|Classic Home w/ Modern Touches / 4 Blks to Subway!|Jovan|Queens|Ozone Park|Entire home/apt|40.67681|-73.86093|86.00|30|6|0.41|2025-05-31|317|1
NY|USD|6613106|Sunny Private Room/Single Bed|Myrta|Brooklyn|Park Slope|Private room|40.66933|-73.98204|70.00|30|55|0.45|2025-04-01|269|1
NY|USD|51782007|Newly Renovated Apt near Parkchester and Bronx Zoo|Elizabeth|Bronx|Parkchester|Entire home/apt|40.83663|-73.86901|80.00|30|3|0.08|2025-04-30|303|1
NY|USD|605051948071435121|Safe Bronx Airbnb with 24-hour Free Street Parking|Summer|Bronx|Pelham Bay|Entire home/apt|40.85060|-73.83687|161.00|30|138|3.58|2023-11-02|275|1
NY|USD|18550867|Luxury Rooms In Pelham Gardens|Dusean|Bronx|Pelham Gardens|Entire home/apt|40.85816|-73.83468|300.00|2|172|1.76|2025-06-09|153|1
NY|USD|31513692|Small Private Bedroom-Great Location in Manhattan|Kyoko|Bronx|Port Morris|Private room|40.80936|-73.93105|58.00|1|79|1.03|2025-06-10|48|2
NY|USD|52771412|Modern Queen Suite / Close to NYC / Parking / Gym|Global Luxury Suites|Bronx|Port Morris|Entire home/apt|40.80989|-73.93061|219.00|1|35|0.79|2025-06-06|364|43
NY|USD|1210389969928838331|Master Bedroom in Shared Home|Serjio|Staten Island|Port Richmond|Private room|40.62764|-74.13564|113.00|2|0|0.00||365|4
NY|USD|3153464|Summers in Sunny 1 BD Prospect Heights Brooklyn|Victoria|Brooklyn|Prospect Heights|Entire home/apt|40.67283|-73.96610|95.00|30|12|0.09|2024-08-23|221|1
NY|USD|20573255|1 Bedroom Apt. Minutes from Manhattan - No Parties|Dora|Brooklyn|Prospect Heights|Private room|40.67950|-73.96575|114.00|30|186|1.96|2024-06-17|92|1
NY|USD|907422793177630168|Economy Single Room at Empire Blvd, Brooklyn|Reservation|Brooklyn|Prospect-Lefferts Gardens|Private room|40.66376|-73.94948|72.00|30|1|0.15|2024-11-24|320|26
NY|USD|763765859890748763|Comfortable bedroom private Home|Deverne|Queens|Queens Village|Private room|40.70864|-73.73303|110.00|30|0|0.00||365|1
NY|USD|784992346056177689|Cozy 4 bed 25 minutes from NYC|Mp|Queens|Queens Village|Entire home/apt|40.71454|-73.73958|225.00|30|4|0.15|2023-10-17|363|2
NY|USD|51888947|Cozy room@Queens! Same area W/Target,Costco|Shogo|Queens|Rego Park|Private room|40.72611|-73.85822|63.00|30|0|0.00||45|231
NY|USD|1142686260926610730|Rego Park Townhouse|Lucky Day Realty Corp.|Queens|Rego Park|Entire home/apt|40.72722|-73.85959|394.00|30|0|0.00||270|9
NY|USD|1266221816823918152|Private Room 5 Min from A Train 1|Michelle|Queens|Richmond Hill|Private room|40.68507|-73.81937|39.00|30|1|0.18|2025-01-06|320|5
NY|USD|910723395719067100|Exposed brick Room, AC, Laundry, Rooftop #364|Eugene|Queens|Ridgewood|Private room|40.70394|-73.90579|37.00|30|0|0.00||364|592
NY|USD|50257676|Peaceful top bedroom in bungalow w/ patio & sauna|Heya|Queens|Rockaway Beach|Private room|40.58810|-73.81427|99.00|30|28|0.80|2023-08-25|177|2
NY|USD|918367184813940843|Surfer's paradise- one block from the beach!|Thomas|Queens|Rockaway Beach|Entire home/apt|40.58664|-73.81375|350.00|30|2|0.11|2024-07-31|224|1
NY|USD|18718195|Private Bedroom in Manhattan|Mariama|Manhattan|Roosevelt Island|Private room|40.76355|-73.94848|140.00|30|92|0.94|2023-11-17|365|1
NY|USD|1266664600275277605|Family-Friendly Home Shared with the Host|Doris|Queens|Rosedale|Private room|40.65443|-73.73677|122.00|1|1|1.00|2025-06-05|361|1
NY|USD|1033633370292103864|Safe Haven (Rosedale Queens) NY|Hubert|Queens|Rosedale|Entire home/apt|40.65121|-73.73571|128.00|30|0|0.00||365|1
NY|USD|991257665855152119|Share this Comfy Guest Suite with Host + parking|Zuhro|Brooklyn|Sheepshead Bay|Entire home/apt|40.58682|-73.94291|224.00|1|32|1.64|2025-04-07|256|1
NY|USD|225976|Cozy multi-leveled apartment!|Marina|Staten Island|Shore Acres|Entire home/apt|40.61019|-74.06757|87.00|30|101|0.60|2025-01-31|199|1
NY|USD|882697572442548483|Soho Stay 4 Single Beds (2Bunk)|Seok Joon|Manhattan|SoHo|Private room|40.71987|-74.00102|291.00|1|7|0.32|2024-02-11|202|18
NY|USD|708883217066615089|SoHo View Room in Hotel Comfort #5|SoHoBlu|Manhattan|SoHo|Entire home/apt|40.71949|-74.00105|389.00|1|8|0.24|2024-12-28|1|17
NY|USD|21369232|Awesome Private Room in Vibrant Bronx|Migdalia|Bronx|Soundview|Private room|40.82502|-73.86046|40.00|30|58|0.63|2025-04-01|185|2
NY|USD|809649506221297892|2023 Development 3 Bedroom NYC Close to JFK!|Courtney|Queens|South Ozone Park|Entire home/apt|40.66348|-73.81511|381.00|30|9|0.33|2023-10-08|350|2
NY|USD|1125396587745754932|Luxurious 3 Bedroom Apt w/Master|Qwasi|Queens|Springfield Gardens|Private room|40.66699|-73.77376|203.00|30|0|0.00||269|1
NY|USD|33127307|COZY BED ROOM 10 MIN FR JFK&TRAIN WITH PVT BATH|Ode|Queens|St. Albans|Private room|40.68514|-73.76958|57.00|5|29|0.38|2022-08-24|134|3
NY|USD|807993496907237876|6 Bedrooms house close to LGA and JFK airport.|Keisha|Queens|St. Albans|Entire home/apt|40.70207|-73.76826|400.00|30|12|0.45|2023-09-25|77|3
NY|USD|34493830|St George Sunny, large 2 bdrm w/yard Monthly only|Mary|Staten Island|St. George|Entire home/apt|40.63947|-74.08374|145.00|30|5|0.07|2024-12-23|90|1
NY|USD|8589729|Magnolia House Saint George|Danforth|Staten Island|St. George|Private room|40.64816|-74.08445|198.00|30|199|1.69|2023-09-25|90|1
NY|USD|1219761487691460780|New York City Apartment Near Free Ferry Boat!|Christian|Staten Island|Stapleton|Private room|40.63280|-74.07582|110.00|1|21|2.14|2025-05-15|286|1
NY|USD|1366730137928867272|Roomrs - Majestic 3 BR Unit in East Village|Maykel|Manhattan|Stuyvesant Town|Entire home/apt|40.73135|-73.98160|305.00|30|0|0.00||33|24
NY|USD|37023920|Simple, Clean, Comfortable place to Sleep|Frank|Manhattan|Stuyvesant Town|Private room|40.73313|-73.97678|843.00|30|0|0.00||270|1
NY|USD|28541768|Cosy small room 15 mins away from Manhattan|Hawk|Queens|Sunnyside|Private room|40.73865|-73.92731|52.00|30|42|0.51|2025-04-30|124|2
NY|USD|1197859177973871671|Cozy & comfy room. 15 Min to Manhattan!|Daniela|Queens|Sunnyside|Private room|40.74525|-73.92315|155.00|2|1|1.00|2025-06-13|258|2
NY|USD|8392929|S Affordable and Cozy in Sunset Park|Isaac, Linda & Noelia|Brooklyn|Sunset Park|Private room|40.65078|-74.00400|42.00|30|44|0.37|2025-05-23|256|4
NY|USD|1275497998968880490|Huge 2 bedroom Brooklyn flat|Jeffrey|Brooklyn|Sunset Park|Entire home/apt|40.65156|-74.00679|130.00|30|3|0.45|2025-05-04|169|2
NY|USD|689090528824596886|Gorgeous One Bed in Midtown West Manhattan|Furnished Quarters|Manhattan|Theater District|Entire home/apt|40.76221|-73.98614|350.00|30|0|0.00||339|133
NY|USD|30387196|Blueground / Times Square, indoor pool|Blueground|Manhattan|Theater District|Entire home/apt|40.76076|-73.98611|448.00|31|0|0.00||246|1054
NY|USD|1132182913168109059|Family Haven / Times Square. Restaurant|M Social Hotel Times Square New |Manhattan|Theater District|Private room|40.76360|-73.98463|633.00|1|22|1.83|2025-05-17|233|6
NY|USD|1090925801241106637|Cozy Bedroom - Beige Suite|Glenys|Bronx|Throgs Neck|Private room|40.81746|-73.81083|50.00|30|1|0.08|2024-06-15|37|4
NY|USD|621880905850087827|Fisherman's Dream on East River & Ferry Point Golf|Book It|Bronx|Throgs Neck|Entire home/apt|40.81221|-73.82663|157.00|30|6|0.17|2024-03-11|365|4
NY|USD|635349981223517857|Charming getaway w/view *Minutes from Ferry*|Joseph|Staten Island|Tompkinsville|Private room|40.63590|-74.07963|184.00|2|99|2.68|2025-06-15|300|1
NY|USD|1297920874380365861|Comfy, cozy, convenient and private - free parking|Amer|Staten Island|Tottenville|Private room|40.52010|-74.23091|102.00|1|44|8.52|2025-06-13|131|1
NY|USD|796476747136417108|Hermosa habitación con baño privado 2|Luci|Bronx|Tremont|Private room|40.84304|-73.89229|114.00|30|5|0.20|2023-08-21|365|3
NY|USD|1121074659882797643|Contemporary Style Escape/ Museums. Bar|Smyth Tribeca New York City|Manhattan|Tribeca|Private room|40.71598|-74.01020|488.00|1|3|0.22|2024-12-23|192|5
NY|USD|23412268|Clean, modern, bright 1 bed/1 bath apt|Edward Anthony|Manhattan|Two Bridges|Entire home/apt|40.71038|-73.99587|100.00|30|74|0.83|2023-03-11|57|2
NY|USD|642193534164032936|Beautiful home. Quiet, comfortable, Parking|Cheryl|Bronx|Unionport|Entire home/apt|40.83238|-73.84960|286.00|3|20|1.07|2025-06-08|253|1
NY|USD|7860155|Room with the view in the heart of NYC|Aldijana|Manhattan|Upper East Side|Private room|40.76339|-73.96739|105.00|30|304|2.56|2025-05-29|294|2
NY|USD|47076840|Charming and pleasant furnished studio apartment|Bridge|Manhattan|Upper East Side|Entire home/apt|40.76048|-73.96175|115.00|70|5|0.10|2024-05-18|213|15
NY|USD|1348466013264464125|Upper East Side Studio|Jennifer|Manhattan|Upper East Side|Entire home/apt|40.77553|-73.95252|175.00|30|0|0.00||234|1
NY|USD|1211672157763868406|Stunning &Cozy 2BR in Prime UES|Saar|Manhattan|Upper East Side|Entire home/apt|40.76546|-73.95840|199.00|30|0|0.00||284|12
NY|USD|1042137021674701046|317-1B Modern 1Br UES W/D|Urban Furnished|Manhattan|Upper East Side|Entire home/apt|40.77240|-73.95447|227.00|30|0|0.00||352|237
NY|USD|1042136838759782915|317-2A Sleek Modern 1Br UES W/D|Urban Furnished|Manhattan|Upper East Side|Entire home/apt|40.77240|-73.95447|232.00|30|0|0.00||339|237
NY|USD|53385771|Lovely 2-bedroom rental unit in New York|Will|Manhattan|Upper East Side|Entire home/apt|40.76653|-73.95136|270.00|30|2|0.05|2023-01-06|364|1
NY|USD|46352910|75-A Stylish Loft W/D Prime Upper east|Urban Furnished|Manhattan|Upper East Side|Entire home/apt|40.77079|-73.95750|336.00|30|14|0.27|2025-01-30|109|237
NY|USD|25346963|Townhouse Apartment steps away from Central Park|Joanne|Manhattan|Upper East Side|Entire home/apt|40.77741|-73.96258|432.00|30|261|3.06|2025-05-06|52|1
NY|USD|1111666023012679487|Opulent Midtown Retreat with White-Gloved Service|RoomPicks|Manhattan|Upper East Side|Hotel room|40.76503|-73.97167|20202.00|1|0|0.00||333|157
NY|USD|1217369903000424628|New Renovated UWS Private Bath Studio Elevator 625|Nat|Manhattan|Upper West Side|Entire home/apt|40.79377|-73.97513|77.00|30|1|0.11|2024-09-15|188|155
NY|USD|980573611700193360|2 bedroom apartment in the Upper westside|Robert|Manhattan|Upper West Side|Private room|40.79361|-73.97110|104.00|4|31|1.85|2025-05-30|35|1
NY|USD|13163474|Cozy, Sunny, UWS Junior One Bedroom|Sami|Manhattan|Upper West Side|Entire home/apt|40.79269|-73.97609|167.00|30|3|0.03|2019-07-28|73|2
NY|USD|12598128|UWS Duplex 3bdrm next to Central Pk|Amy|Manhattan|Upper West Side|Entire home/apt|40.79879|-73.95972|200.00|30|5|0.05|2019-08-12|285|17
NY|USD|722492556613373205|Monthly rental in Manhattan!|Victor,Sebastian|Manhattan|Upper West Side|Entire home/apt|40.79452|-73.97467|215.00|30|32|0.99|2023-12-03|365|5
NY|USD|1112351229041577978|Spacious 4BR/2BA duplex apt w/ private patio, W/D|Inna|Manhattan|Upper West Side|Entire home/apt|40.79531|-73.96293|328.00|30|4|0.31|2025-04-18|322|49
NY|USD|52916520|Belleclaire, Newly renovated Deluxe 2 Queen Beds|Hotel Belleclaire|Manhattan|Upper West Side|Private room|40.78162|-73.98042|439.00|1|2|0.05|2023-05-31|321|11
NY|USD|1094808192959006232|Deluxe Room In The Upper West Side, NYC!|RoomPicks|Manhattan|Upper West Side|Hotel room|40.78347|-73.97849|40000.00|1|0|0.00||328|157
NY|USD|20849101|NYC private room in Private house/Mt Vernon area|Tunzil|Bronx|Wakefield|Private room|40.90194|-73.84236|70.00|30|10|0.11|2018-02-13|89|2
NY|USD|45399393|Spacious Comfortably 2 Bedrooms Apartment|Andrea|Bronx|Wakefield|Entire home/apt|40.88332|-73.85018|257.00|30|7|0.12|2022-01-01|269|1
NY|USD|847087780844574910|Washington Bridge take you to The Bronk|Hiroki|Manhattan|Washington Heights|Private room|40.85431|-73.92621|44.00|30|4|0.18|2024-09-02|45|254
NY|USD|1288223732677733582|Subway 181 St station is 3min walk|Hiroki|Manhattan|Washington Heights|Private room|40.84953|-73.94071|50.00|30|0|0.00||45|254
NY|USD|38954037|Comfort zone by hospital|Jessica|Manhattan|Washington Heights|Private room|40.84526|-73.94270|51.00|30|16|0.37|2025-02-18|76|3
NY|USD|1065769971445201312|3 Pet-friendly Queen Rooms at Radio Hotel!|RoomPicks|Manhattan|Washington Heights|Hotel room|40.84775|-73.93153|989.00|1|1|0.09|2024-07-05|365|157
NY|USD|53027258|Room Near RUMC (2nd floor, Room 2)|Ervis|Staten Island|West Brighton|Private room|40.63453|-74.11554|50.00|30|15|0.35|2024-12-25|360|7
NY|USD|212109|Charming Greenwich Village share|Susan|Manhattan|West Village|Private room|40.73814|-74.00811|98.00|31|13|0.11|2024-08-10|311|1
NY|USD|51176539|Private & Peaceful / Entire Place / High-Line|Terry|Manhattan|West Village|Entire home/apt|40.73851|-74.00750|230.00|30|0|0.00||365|1
NY|USD|1264336109140021594|Blueground / West Village, w/d & a/c, nr Chelsea|Blueground|Manhattan|West Village|Entire home/apt|40.73732|-74.00424|757.00|31|0|0.00||341|1054
NY|USD|35496842|Private, cozy, comfortable, it feels like home|Segun|Bronx|Williamsbridge|Entire home/apt|40.88151|-73.84940|67.00|30|74|1.01|2025-03-22|289|1
NY|USD|639920141110609277|Cozy Room / Bronx NY / Traveling Professionals|Jonathan|Bronx|Williamsbridge|Private room|40.87977|-73.85488|75.00|30|40|1.09|2023-08-29|87|3
NY|USD|1153504728938516631|New Private Room in Brooklyn|Mimi|Brooklyn|Williamsburg|Private room|40.70138|-73.94071|67.00|30|0|0.00||337|5
NY|USD|26925980|Cozy Williamsburg room|Russell|Brooklyn|Williamsburg|Private room|40.70833|-73.96582|82.00|1|238|2.84|2025-06-07|24|3
NY|USD|807945843450389326|Brick Wall Room near by Williamsburg Bridge|Sercan|Brooklyn|Williamsburg|Private room|40.70903|-73.96154|176.00|30|10|0.35|2025-05-13|227|1
NY|USD|942039635983891486|Beautiful Williamsburg Apt|Natalia|Brooklyn|Williamsburg|Entire home/apt|40.71245|-73.95752|230.00|30|6|0.32|2025-05-29|170|1
NY|USD|1086396757887111907|Sunny 1 bed in Luxury Building|Melissa|Brooklyn|Williamsburg|Entire home/apt|40.71329|-73.96756|240.00|30|0|0.00||269|2
NY|USD|53823703|2 Bed Loft- Firehouse from 1920s - Healing Energy|Greg|Brooklyn|Williamsburg|Entire home/apt|40.71693|-73.96274|410.00|30|18|0.44|2023-09-02|65|1
NY|USD|17189941|Modern condo in the heart of Williamsburg|Levent|Brooklyn|Williamsburg|Entire home/apt|40.71558|-73.95721|450.00|30|11|0.12|2023-10-20|107|1
NY|USD|31053180|Modern 2BR Williamsburg Loft With a Balcony|Alex|Brooklyn|Williamsburg|Entire home/apt|40.70765|-73.94257|544.00|90|8|0.10|2020-03-14|365|3
NY|USD|1095642526311759724|Modern Loft w/ Rooftop & Manhattan Views|RoomPicks|Brooklyn|Williamsburg|Hotel room|40.72296|-73.95766|40000.00|1|0|0.00||67|157
NY|USD|449660|Art & Music Salon|Elizabeth|Brooklyn|Windsor Terrace|Private room|40.64885|-73.97453|72.00|30|125|0.78|2019-11-03|365|1
NY|USD|53817954|Woodhaven suite near the Forest Park|Rosie|Queens|Woodhaven|Private room|40.69438|-73.85424|45.00|30|49|1.20|2025-02-08|226|1
NY|USD|47874401|Beautiful One Bedroom|José|Queens|Woodhaven|Entire home/apt|40.69224|-73.86665|256.00|30|91|1.77|2023-12-10|269|1
NY|USD|49500273|Renovated 2021@Queens apartment.|Shogo|Queens|Woodside|Private room|40.74076|-73.89341|56.00|30|35|0.71|2023-09-05|31|231
NY|USD|915896747275039057|Penthouse Duplex Apartment NYC|Khizer|Queens|Woodside|Entire home/apt|40.74568|-73.89905|256.00|2|89|3.66|2025-06-11|206|1
RJ|BRL|1314705004375821855|Casúlo das Deusas|Sarah||Alto da Boa Vista|Private room|-22.97665|-43.28266|145.00|1|1|0.38|2025-01-02|365|1
RJ|BRL|696372343844715863|Curta o Rio junto a natureza da Floresta da Tijuca|Geni||Alto da Boa Vista|Entire home/apt|-22.97465|-43.28126|342.00|1|0|0.00||365|1
RJ|BRL|775576743162057084|Quarto em Apartamento Bucólico na Tijuca|Felipe||Andaraí|Private room|-22.91947|-43.24976|92.00|1|5|0.19|2024-05-05|323|1
RJ|BRL|54020259|Cond resid com elevador rua tranquila.|Sandra||Andaraí|Entire home/apt|-22.92339|-43.24335|293.00|1|23|0.67|2025-03-05|361|1
RJ|BRL|1142939745359134097|Excelente casa pra fim de semana|Cleber Henrique||Anil|Entire home/apt|-22.95654|-43.34203|141.00|1|0|0.00||270|1
RJ|BRL|1173311149012331073|GHCasadamontanha-Q6-Wc Privativo|Juliana||Anil|Private room|-22.94821|-43.33343|152.00|1|0|0.00||90|2
RJ|BRL|1289201993908130737|Apartamento Ilha do Governador|Stefany||Bancários|Entire home/apt|-22.79832|-43.17315|256.00|1|1|0.38|2025-01-02|179|2
RJ|BRL|837048493277163071|Quarto privativo - Barra - II|Claudia||Barra da Tijuca|Private room|-23.00532|-43.34972|167.00|3|51|2.07|2025-03-04|352|3
RJ|BRL|1359470944124967338|Mediterrâneo Flat 501-2|Fábio||Barra da Tijuca|Entire home/apt|-23.00463|-43.34115|247.00|2|1|1.00|2025-02-23|170|2
RJ|BRL|1199539392037252202|Quarto para mulheres em condomínio próximo à praia|Maristela||Barra da Tijuca|Private room|-23.01112|-43.42041|300.00|1|1|0.16|2024-09-15|269|2
RJ|BRL|1212486179339823420|Estilo e Conforto-Praia da Barra|Luis Carlos||Barra da Tijuca|Entire home/apt|-23.01067|-43.37055|333.00|2|16|2.15|2025-01-13|269|3
RJ|BRL|1349232820678800591|2 quartos estacionamento e estrutura de resort|Catia||Barra da Tijuca|Entire home/apt|-23.00530|-43.32134|383.00|2|1|1.00|2025-03-06|74|7
RJ|BRL|1211535853465575901|Otimo ape em frente ao Downtown|Daniela||Barra da Tijuca|Entire home/apt|-23.00585|-43.32488|399.00|7|0|0.00||365|19
RJ|BRL|14280138|Excelente localização, com estrutura de resort!|Mara||Barra da Tijuca|Entire home/apt|-22.99825|-43.41498|400.00|2|64|0.61|2025-01-19|90|1
RJ|BRL|1220194855279336701|Apto/hotel Barraleme pé na areia|Bianca||Barra da Tijuca|Entire home/apt|-23.01459|-43.30319|450.00|3|6|2.69|2025-03-03|267|3
RJ|BRL|1305175228523311574|Apto 2qts, prox a praia e metro|Cassiana||Barra da Tijuca|Entire home/apt|-23.00399|-43.32495|500.00|2|2|0.78|2025-03-05|365|1
RJ|BRL|1048608215872055666|Studio Frente ao Mar Posto 7|Sonia Iris||Barra da Tijuca|Entire home/apt|-23.01088|-43.35453|590.00|1|10|0.67|2025-03-04|175|7
RJ|BRL|1170299194818715703|Room - Barra da Tijuca Beach, luxury and security|Maria||Barra da Tijuca|Entire home/apt|-23.00947|-43.33276|690.00|2|4|0.42|2024-06-14|289|2
RJ|BRL|54390638|Aconchego carioca a 10 min da praia a pé|Lívia||Barra da Tijuca|Entire home/apt|-23.00382|-43.34846|713.00|1|48|1.29|2025-01-04|258|1
RJ|BRL|1280469636445340609|Apartamento na Praia do Pepê|Laura||Barra da Tijuca|Entire home/apt|-23.01362|-43.31051|849.00|2|7|2.69|2025-03-05|175|1
RJ|BRL|1371985411029843574|Apartamento aconchegante a beira-mar|Andrea||Barra da Tijuca|Entire home/apt|-23.01002|-43.35814|900.00|1|0|0.00||313|2
RJ|BRL|904356583041842910|Suíte p. até 4 pessoas na Gigóia Frente pra Lagoa|Beach House Gigóia||Barra da Tijuca|Private room|-23.00329|-43.30790|968.00|1|0|0.00||12|6
RJ|BRL|1083951512397971223|Cobertura duplex em frente a praia e com 3 suites|Neusa Silveira||Barra da Tijuca|Entire home/apt|-23.00816|-43.36551|1500.00|2|3|0.48|2025-03-05|362|1
RJ|BRL|868162300279910849|vista da Lagoa da Barra e Pedra da Gávea.|Stefan||Barra da Tijuca|Entire home/apt|-23.00301|-43.34746|1600.00|2|0|0.00||358|6
RJ|BRL|1289312827563728725|Beach Front Apartment in Rio|Ariel||Barra da Tijuca|Entire home/apt|-23.00997|-43.37175|1982.00|4|1|1.00|2025-03-03|312|1
RJ|BRL|13124436|Best Place During the Olympics.|Marion||Barra da Tijuca|Entire home/apt|-23.01006|-43.33389|4248.00|10|0|0.00||358|1
RJ|BRL|48505313|Casa de Canal 20 pessoas , Barra da Tijuca|Alan||Barra da Tijuca|Entire home/apt|-23.01205|-43.29976|25000.00|1|0|0.00||365|35
RJ|BRL|557104283273662629|Quarto Pousada Colina do Canto - Apto 2|Solano Rodrigo||Barra de Guaratiba|Private room|-23.07200|-43.56860|230.00|2|6|0.16|2025-03-04|365|5
RJ|BRL|51109748|Recanto do pôr do sol|Luciano||Barra de Guaratiba|Entire home/apt|-23.06480|-43.56377|259.00|3|98|2.31|2025-03-16|180|1
RJ|BRL|858965489598883424|Quarto|Tulio||Benfica|Private room|-22.89726|-43.23977|208.00|1|2|2.00|2025-03-09|179|1
RJ|BRL|14090499|Available to Olympics. Family atmosphere.|Newton||Bento Ribeiro|Entire home/apt|-22.86143|-43.35825|187.00|7|0|0.00||364|1
RJ|BRL|40640247|Quarto 302 top|Marcelo||Bonsucesso|Private room|-22.86159|-43.25521|63.00|2|3|0.12|2025-01-10|365|13
RJ|BRL|1372763728521863942|02 Novo Hostel a 10 min de Copacabana|Pablo||Botafogo|Shared room|-22.95610|-43.18350|120.00|1|0|0.00||321|25
RJ|BRL|53967455|Charming peaceful nest in between Jungle and City|Bryan||Botafogo|Private room|-22.94352|-43.18931|165.00|1|7|0.18|2023-03-08|362|1
RJ|BRL|592527757875770233|Alugo quarto em frente ao metrô de Botafogo|Esther||Botafogo|Private room|-22.94911|-43.18336|221.00|2|15|0.44|2025-03-08|155|4
RJ|BRL|9878322|Cobertura charmosa a uma quadra do metrô.|Edmundo||Botafogo|Entire home/apt|-22.94960|-43.18681|297.00|15|57|0.75|2024-11-21|63|1
RJ|BRL|32467917|Botafogo, 100m do Metrô, piscina e estacionamento|Jorge||Botafogo|Entire home/apt|-22.94900|-43.18593|379.00|5|8|0.11|2025-01-02|107|1
RJ|BRL|998812938155102289|excelente apto ótima.localizacao|Regina||Botafogo|Entire home/apt|-22.95343|-43.17840|387.00|1|3|0.17|2025-03-05|260|1
RJ|BRL|15234870|Private room with Queen & Mez. Bed - Best Location|Izilda||Botafogo|Entire home/apt|-22.93999|-43.18099|400.00|1|2|0.02|2018-01-02|364|4
RJ|BRL|791896101798278614|Suíte do sobrado.|Elcio||Brás de Pina|Private room|-22.82577|-43.28382|167.00|2|4|0.26|2025-03-14|88|4
RJ|BRL|677491276191936727|Apartamento completo|Simone||Cachambi|Entire home/apt|-22.89657|-43.26791|200.00|2|60|1.88|2025-03-05|19|1
RJ|BRL|1275264109336550807|Suíte na Barra c/ Garagem e Self Check-in 1BL2|Mirlane||Camorim|Entire home/apt|-22.98005|-43.42160|157.00|1|30|6.47|2025-03-17|23|14
RJ|BRL|1198815806531849094|Rio centro suíte vista incrível|Patricia||Camorim|Entire home/apt|-22.98052|-43.42267|171.00|1|13|1.77|2025-03-06|90|18
RJ|BRL|1164806644845153473|apart 2 quartos na barra RJ|Rosane||Camorim|Entire home/apt|-22.98462|-43.42511|180.00|2|30|4.02|2025-03-16|172|1
RJ|BRL|1238650791309253656|Loft _Riocentro_Farmasi Arena_Barra da Tijuca|Katiane||Camorim|Entire home/apt|-22.98346|-43.41514|220.00|3|4|0.67|2025-03-06|133|1
RJ|BRL|570181567723975746|Local bem tranquilo proximo ao Park shopping|Vegiel||Campo Grande|Private room|-22.94167|-43.57104|86.00|1|21|0.63|2024-02-12|365|1
RJ|BRL|50898661|Aconchegante|Alexandre||Campo Grande|Entire home/apt|-22.87557|-43.56482|216.00|2|31|0.70|2025-03-16|100|5
RJ|BRL|1363422284696343300|Authentic RJ Hostel Experience (Bed 6)|Daniel||Catete|Shared room|-22.92761|-43.17713|54.00|1|0|0.00||361|7
RJ|BRL|1274295730448206448|Apartamento próximo à praia|Gabriela||Catete|Entire home/apt|-22.92649|-43.17688|135.00|3|5|1.92|2025-03-07|68|1
RJ|BRL|746236668771915495|Casa agradável com pátio e vista do Pão de Açúcar|Nairobi||Catete|Entire home/apt|-22.92452|-43.18204|350.00|4|0|0.00||353|1
RJ|BRL|1021807165000765602|LaVie en Fleur|Gabriela||Catete|Private room|-22.92949|-43.17862|373.00|1|59|3.60|2025-03-02|71|4
RJ|BRL|30882039|Temporada sala, dois quartos +1 serviço GLÓRIA|Aurélio||Catete|Entire home/apt|-22.92311|-43.17911|563.00|4|6|0.08|2024-04-18|364|1
RJ|BRL|42200384|Super confortável studio em Lapa.|Marcos||Centro|Entire home/apt|-22.91376|-43.18168|90.00|3|66|1.07|2025-02-18|126|1
RJ|BRL|862443832551256208|Quarto Alecrim com varanda no Centro|Anderson||Centro|Private room|-22.90926|-43.17187|90.00|2|77|3.29|2025-03-12|252|6
RJ|BRL|1053058388191118583|Quarto Privativo 5 /Cinelândia RJ|Paulo Cesar Garcia||Centro|Private room|-22.91110|-43.17636|124.00|1|3|0.25|2024-08-17|66|9
RJ|BRL|1274690422519235401|Beautiful Apartment in the Heart of Rio - Lapa|Marcus||Centro|Entire home/apt|-22.91310|-43.18127|143.00|3|0|0.00||215|5
RJ|BRL|568916759943283626|Apartamento-Kitnete, bairro da Lapa, RJ|Nilson||Centro|Entire home/apt|-22.91373|-43.18401|150.00|5|5|0.14|2024-11-23|364|1
RJ|BRL|1253017457540318652|Studio Magnífico no Centro do RJ|Robert||Centro|Entire home/apt|-22.90019|-43.18191|158.00|1|44|7.54|2025-03-10|76|2
RJ|BRL|1039224693716327024|Studio Lapa House com vista|Mauricio Henrique||Centro|Entire home/apt|-22.90966|-43.18422|163.00|1|68|4.36|2025-03-10|33|1
RJ|BRL|1331181174699511089|apartamento centro do rio de janeiro|Morganna||Centro|Entire home/apt|-22.90411|-43.18144|182.00|2|1|1.00|2025-03-03|357|1
RJ|BRL|1176137403442455412|Estúdio Moderno no Coraçao do Rio de Janeiro|Mauro Sergio||Centro|Entire home/apt|-22.90113|-43.17954|288.00|3|15|1.74|2025-03-01|47|1
RJ|BRL|778096757279698712|Flat piscina lapa co-working|Débora||Centro|Entire home/apt|-22.91165|-43.17797|300.00|3|9|0.33|2024-02-18|263|13
RJ|BRL|30591052|Centro Excelente Sala e Quarto, frente 4/8 pessoas|Josy||Centro|Entire home/apt|-22.91259|-43.17649|450.00|1|12|0.16|2025-03-04|360|4
RJ|BRL|816034468702507102|Lindo AP Estácio/Centro prox do metrô.|Bernardo||Cidade Nova|Entire home/apt|-22.91232|-43.20211|250.00|2|20|0.79|2025-03-12|363|2
RJ|BRL|38019250|Suíte a 50m estação Metrô General Osório Ipanema|Thiago||Copacabana|Private room|-22.98091|-43.19402|121.00|5|53|0.84|2025-03-02|206|6
RJ|BRL|41074176|Aconchego carioca super promoção março e abril|Petruza||Copacabana|Entire home/apt|-22.96751|-43.18437|136.00|3|164|2.61|2025-03-08|47|6
RJ|BRL|1341394161684472111|305 Conjugado em Copacabana|Elizete||Copacabana|Entire home/apt|-22.96321|-43.17502|146.00|2|2|2.00|2025-03-19|145|11
RJ|BRL|776805461268846539|Suite solteiro praia Copa posto 5|Sophie||Copacabana|Entire home/apt|-22.98072|-43.19137|150.00|2|70|2.54|2025-03-17|18|1
RJ|BRL|1197077175320537905|Copacabana 4min do mar Posto 2|Rosimeire Jacinta De||Copacabana|Entire home/apt|-22.96735|-43.18138|180.00|3|18|2.52|2025-03-18|306|1
RJ|BRL|1146256166444572728|Apartamento em Copacabana, perto da praia|Keyla||Copacabana|Entire home/apt|-22.96284|-43.17731|180.00|1|5|0.51|2025-01-04|20|1
RJ|BRL|41829545|Apartamento conforto coracao de Copacabana|Rita Maria De Sousa E||Copacabana|Entire home/apt|-22.97779|-43.19271|190.00|2|10|0.16|2025-03-05|27|1
RJ|BRL|1049232630394432491|Nice room close to the beach.|Mateusz||Copacabana|Private room|-22.97060|-43.19087|199.00|50|0|0.00||269|1
RJ|BRL|1228202053499150531|Studio in Copacabana by the beach / NSC 1085/307|Omar Do Rio||Copacabana|Entire home/apt|-22.97896|-43.19196|205.00|1|8|1.26|2025-03-09|330|214
RJ|BRL|13544020|Studio em Copacabana no Posto 3 - Quase na praia.|Eumir||Copacabana|Entire home/apt|-22.96905|-43.18226|206.00|3|301|2.87|2025-02-20|50|1
RJ|BRL|30469824|Lindo apartamento, vista pro Cristo em Copacabana!|Tatiana||Copacabana|Entire home/apt|-22.96821|-43.18961|220.00|2|0|0.00||45|6
RJ|BRL|1049944745816585306|Luxuoso apartamento Copacabana.|Jakeline||Copacabana|Entire home/apt|-22.96827|-43.18370|220.00|5|59|4.17|2025-03-10|161|6
RJ|BRL|1006500396963880688|Lima602 / Apt 500 m from Copacabana beach|Estadia||Copacabana|Entire home/apt|-22.98395|-43.19327|222.00|1|6|0.37|2025-02-14|190|124
RJ|BRL|1376977380561741380|Top Copacabana posto 6|Ana Cristina||Copacabana|Entire home/apt|-22.98221|-43.19277|234.00|6|0|0.00||327|8
RJ|BRL|1379131042440157763|Apartamento Loft próximo a praia|Elisabete||Copacabana|Entire home/apt|-22.96696|-43.18361|239.00|2|0|0.00||345|5
RJ|BRL|42334670|C3+ Conforto em Copacabana Junto a PRAIA & METRÔ|Renata||Copacabana|Entire home/apt|-22.96508|-43.17773|257.00|1|119|1.94|2025-02-18|274|17
RJ|BRL|1354625134924254485|Suite Master com Ar-condicionado 3 min Praia Copaa|Rogério||Copacabana|Private room|-22.96471|-43.17949|259.00|1|1|1.00|2025-03-06|365|11
RJ|BRL|792192519365315561|Copacabana - 250m da praia - pra até 4 hóspedes|Mellany||Copacabana|Entire home/apt|-22.97776|-43.19104|259.00|4|10|0.37|2024-09-22|122|8
RJ|BRL|6335642|Stunning Studio in Copacabana|André||Copacabana|Entire home/apt|-22.96466|-43.17541|260.00|4|68|0.60|2025-03-05|42|4
RJ|BRL|2984779|Estúdio Copacabana Posto 6|Carmen||Copacabana|Entire home/apt|-22.98366|-43.19085|262.00|2|202|2.65|2025-03-10|135|1
RJ|BRL|1379531413292341793|Studio na Praia de Copacabana e vista da floresta|Andre||Copacabana|Entire home/apt|-22.96352|-43.17811|268.00|3|0|0.00||341|2
RJ|BRL|38461157|Copacabana lindo apartamento super bem localizado|Edmir||Copacabana|Entire home/apt|-22.96452|-43.17862|269.00|2|35|0.52|2025-03-12|71|1
RJ|BRL|19924565|Mary Schorr- quarto Amarelo (03 Pax) Copacabana.|Maria||Copacabana|Private room|-22.98046|-43.19430|276.00|3|14|0.34|2025-03-04|74|4
RJ|BRL|51901735|Apto super aconchegante a 1 quadra da praia ❤|Maria Luisa||Copacabana|Entire home/apt|-22.98404|-43.19147|276.00|2|55|1.31|2025-02-23|169|33
RJ|BRL|1355827622428983127|Apartamento Simples e Bem Localizado no Leme|Vagner||Copacabana|Entire home/apt|-22.96139|-43.17497|279.00|2|3|3.00|2025-03-05|53|32
RJ|BRL|1363880017332122892|COPAleme!|Ana Maria||Copacabana|Entire home/apt|-22.96536|-43.17653|309.00|1|1|1.00|2025-03-14|324|1
RJ|BRL|1252508356511528602|CopaLeme Princesa do mar|Aline||Copacabana|Entire home/apt|-22.96348|-43.17390|310.00|2|4|1.26|2025-03-07|6|4
RJ|BRL|1047031193142544984|Aptº 3ªqd.praia Copacabana,máx.4 pessoas,5 noites+|Rossali||Copacabana|Entire home/apt|-22.96286|-43.17561|313.00|5|1|1.00|2025-03-05|39|1
RJ|BRL|559456184149832988|Apartamento silencioso - praia - Palacete Ipanema|Fabiano||Copacabana|Entire home/apt|-22.98564|-43.19138|315.00|2|94|2.66|2025-03-19|70|12
RJ|BRL|1056469848845949950|Excelente apartamento próximo à praia|Lily||Copacabana|Entire home/apt|-22.97277|-43.18826|317.00|2|6|0.45|2025-02-22|108|1
RJ|BRL|1350736399544944960|Charmoso Studio ao lado da praia de Copacabana!|Deborah||Copacabana|Entire home/apt|-22.96436|-43.17447|325.00|1|4|3.75|2025-03-05|276|1
RJ|BRL|1074442086316051711|Apartamento em Copacabana - posto 3|Carlos Alberto||Copacabana|Entire home/apt|-22.96568|-43.18284|338.00|2|0|0.00||261|1
RJ|BRL|1254585150944504592|Lovely room in Copa|Marielle||Copacabana|Private room|-22.97749|-43.19061|340.00|3|0|0.00||1|2
RJ|BRL|1030409535328769737|Apt 2 quadras da praia de Copacabana|Mariana||Copacabana|Entire home/apt|-22.96277|-43.17906|342.00|3|6|0.42|2025-01-13|110|1
RJ|BRL|1323138052278250498|3 minutes from Copacabana beach / Du705|Fabio||Copacabana|Entire home/apt|-22.98014|-43.18955|344.00|1|5|2.31|2025-03-08|293|41
RJ|BRL|1015177706879927519|Loft 33 - Praia, conforto e praticidade|Maria De Fatima||Copacabana|Entire home/apt|-22.96451|-43.17703|346.00|2|67|4.04|2025-03-13|73|1
RJ|BRL|33243195|Apartamento com vista lateral para o mar reformado|Sabrina||Copacabana|Entire home/apt|-22.96497|-43.17851|350.00|3|7|0.10|2023-02-16|297|4
RJ|BRL|1225575174531322729|2 quartos casal|Bianca||Copacabana|Private room|-22.97523|-43.19311|405.00|3|0|0.00||358|1
RJ|BRL|1320660507733491696|Ap no coração de Copacabana|Sheyla||Copacabana|Entire home/apt|-22.96801|-43.18290|418.00|3|0|0.00||359|6
RJ|BRL|720276797831346352|Beautiful view in a two bedrooms in Copacabana|Wanusa||Copacabana|Entire home/apt|-22.96455|-43.17785|436.00|3|56|1.89|2025-03-04|239|2
RJ|BRL|19501701|Residencial Princess Copacabana|Sergio||Copacabana|Entire home/apt|-22.97696|-43.19199|450.00|3|15|0.62|2025-03-15|5|2
RJ|BRL|1310249380078304322|Aconchegante e luminoso ap|Maria||Copacabana|Entire home/apt|-22.96339|-43.17391|455.00|1|1|0.40|2025-01-05|332|2
RJ|BRL|1358947457870319174|Conforto, modernidade e ótima localização|Fabio||Copacabana|Entire home/apt|-22.97740|-43.19431|467.00|4|0|0.00||36|3
RJ|BRL|819702735496491736|Quarto suíte em Copacabana|Carlos Sérgio||Copacabana|Private room|-22.97170|-43.18978|489.00|4|2|0.12|2023-11-22|360|3
RJ|BRL|409038|Copa Penthouse - 2 Bed Apartment|Bob||Copacabana|Entire home/apt|-22.96697|-43.18053|500.00|3|13|0.10|2024-02-17|1|6
RJ|BRL|25399625|one block from copacabana beach|Sueli||Copacabana|Entire home/apt|-22.97374|-43.18698|500.00|4|5|0.08|2023-02-24|74|1
RJ|BRL|1280434013096443935|Sua casa em Copacabana.|Pedro||Copacabana|Entire home/apt|-22.96488|-43.19204|524.00|2|6|1.44|2025-03-05|115|1
RJ|BRL|1055229125284093398|Cobertura linda vista|Marcos||Copacabana|Entire home/apt|-22.97607|-43.18883|540.00|1|0|0.00||363|2
RJ|BRL|971194431240837106|Apartamento de 2 quartos em Copacabana|Bruno||Copacabana|Entire home/apt|-22.96588|-43.18485|600.00|2|9|0.48|2025-03-06|78|1
RJ|BRL|879102060702438421|Apartamento beira-mar na Praia de Copacabana|Maria Do Carmo||Copacabana|Entire home/apt|-22.96841|-43.18063|629.00|5|19|1.33|2025-03-07|293|1
RJ|BRL|1067490758258736281|Apartamento Aconchegante e Perto da Praia|Raíra||Copacabana|Entire home/apt|-22.98487|-43.19243|707.00|3|2|0.15|2025-03-04|6|2
RJ|BRL|9049668|Spacious! Privileged location in Copacabana.|Vera||Copacabana|Entire home/apt|-22.97806|-43.19014|730.00|2|33|0.31|2025-03-10|46|6
RJ|BRL|997257474011287223|Apartamento no Arpoador|Pedro||Copacabana|Entire home/apt|-22.98428|-43.19275|765.00|5|47|2.88|2025-03-03|184|27
RJ|BRL|1234749915562610432|Copacabana - Ipanema - Arpoador|Geraldo & Simone||Copacabana|Entire home/apt|-22.98427|-43.19388|826.00|3|3|0.76|2025-03-10|181|12
RJ|BRL|9666419|Espetacular Apto Praia de Copacabana|Monica||Copacabana|Entire home/apt|-22.96495|-43.17819|900.00|3|4|0.04|2023-11-20|135|1
RJ|BRL|680243006926112097|Cozy flat 2 COPACABANA best area 3min beach⛱+metro|Pierre||Copacabana|Entire home/apt|-22.97601|-43.19073|904.00|2|16|0.52|2025-03-07|82|2
RJ|BRL|1076007360937301736|Copanema apartamento charmoso, 2 quadras praia|Bruno||Copacabana|Entire home/apt|-22.98175|-43.19286|909.00|3|1|1.00|2025-03-06|200|1
RJ|BRL|1122109690956074015|Pé na Areia 2|Carolinna||Copacabana|Entire home/apt|-22.97258|-43.18562|966.00|2|14|1.63|2025-02-24|267|7
RJ|BRL|32639836|Lar doce lar, 5 min da praia|Thiago||Copacabana|Entire home/apt|-22.97221|-43.19185|1000.00|2|10|0.68|2025-03-04|167|1
RJ|BRL|10742845|3 quartos com vaga no melhor ponto de Copacabana|Mirelli||Copacabana|Entire home/apt|-22.97618|-43.19144|1000.00|7|4|0.16|2024-09-23|86|5
RJ|BRL|1365293569275056264|Quarto amplo, comporta 4 pessoas|Francisco||Copacabana|Private room|-22.97467|-43.19374|1000.00|1|0|0.00||353|3
RJ|BRL|2856511|Apartamento Carnaval copacabana!!!|Lilien||Copacabana|Entire home/apt|-22.96500|-43.18688|1000.00|2|1|1.00|2025-03-03|122|15
RJ|BRL|42363724|LINDO APTO. FRENTE MAR. AMZNG SEA FRONT APRT.|Bruno||Copacabana|Entire home/apt|-22.97156|-43.18529|1051.00|4|105|1.71|2025-02-24|286|3
RJ|BRL|557702185434257104|Cob Copacabana alto padrão, Top|Marcelo||Copacabana|Entire home/apt|-22.96696|-43.18217|1080.00|1|0|0.00||267|2
RJ|BRL|41235265|VISTA FRONTAL TOTAL, 3QTS. AMZN SEA VIEW, 3BDR.|Bruno||Copacabana|Entire home/apt|-22.97149|-43.18440|1275.00|4|130|2.08|2025-02-16|266|3
RJ|BRL|1069827498316799002|Apto a uma quadra da praia !|Magda Medrado De Aguiar||Copacabana|Entire home/apt|-22.98242|-43.19116|1643.00|3|27|2.77|2025-03-10|235|1
RJ|BRL|1349434979122422934|Copacabana carnaval|Ana Carolina||Copacabana|Entire home/apt|-22.97562|-43.18967|1710.00|5|1|1.00|2025-03-05|353|2
RJ|BRL|52613577|Maravilhoso apartamento de 3 quartos em Copacabana|Luis Henrique||Copacabana|Entire home/apt|-22.97015|-43.18590|2134.00|10|0|0.00||179|4
RJ|BRL|1050670576941883314|Boutique 2BR Oasis Copacabana Ocean Views|Unhotel||Copacabana|Entire home/apt|-22.97427|-43.18545|2375.00|1|22|1.53|2025-02-20|291|54
RJ|BRL|1013961266195030705|Promoção: TOPEndereço Copacabana|Cesar||Copacabana|Entire home/apt|-22.97470|-43.18959|9600.00|1|0|0.00||270|22
RJ|BRL|779951|Wonderful green Loft in Santa Teresa, Rio|Portão Vermelho||Cosme Velho|Entire home/apt|-22.94350|-43.20410|236.00|30|21|0.20|2025-03-10|323|3
RJ|BRL|1161645033881726578|Vista espetacular, melhor do Rio - Iseda|Renata||Cosme Velho|Private room|-22.94010|-43.20001|261.00|2|2|0.32|2024-09-22|270|5
RJ|BRL|696423251739546464|Ap próximo ao Rock in Rio|Juliana||Curicica|Entire home/apt|-22.96270|-43.40277|557.00|4|1|0.03|2022-09-12|88|1
RJ|BRL|1160216539007075891|Apto - Shopping Novamerica|Pedro||Del Castilho|Entire home/apt|-22.87459|-43.26628|611.00|3|0|0.00||87|1
RJ|BRL|688396092911497447|Quarto de casal|Mara||Encantado|Entire home/apt|-22.89756|-43.30230|154.00|2|6|0.20|2024-03-27|78|21
RJ|BRL|1026804376400801510|ap 208|Mara||Encantado|Private room|-22.89813|-43.30455|250.00|1|2|0.76|2025-02-12|11|21
RJ|BRL|1322245704803760478|021 Pousada Maanaim muito AMOR|Alexandre||Engenho Novo|Shared room|-22.90527|-43.26872|68.00|1|1|1.00|2025-03-06|365|12
RJ|BRL|1007291424501199324|Boas Energias e Acolhimento|Alice Maria||Engenho Novo|Private room|-22.91399|-43.27149|146.00|1|6|0.36|2025-02-28|78|1
RJ|BRL|582738361020435116|apto próximo Maracanã, Sambodromo e Rodoviária.|Stefan||Engenho Novo|Entire home/apt|-22.89999|-43.27071|300.00|2|0|0.00||292|6
RJ|BRL|605673759315666322|Varandas do Engenhão A|Adriana||Engenho de Dentro|Private room|-22.89071|-43.29396|700.00|1|11|0.45|2024-10-21|362|2
RJ|BRL|1352050849880822101|Engenhão - Ao lado Show Shakira|Silvania||Engenho de Dentro|Entire home/apt|-22.89035|-43.29133|1349.00|1|1|0.81|2025-02-12|51|6
RJ|BRL|1056126283619663123|Aluguel de quarto de solteiro|Eduardo Henrique||Estácio|Private room|-22.91649|-43.20851|135.00|1|0|0.00||269|1
RJ|BRL|32568075|Apartamento confortável no coração do Rio .|Fernanda||Estácio|Entire home/apt|-22.91525|-43.20039|800.00|3|2|0.03|2025-03-03|364|1
RJ|BRL|13695446|Apartamento aconchegante no Flamengo|Larissa||Flamengo|Entire home/apt|-22.92980|-43.17478|167.00|3|28|0.27|2023-04-11|82|2
RJ|BRL|564928990492549661|Apartamento com vista espetacular|Regina||Flamengo|Entire home/apt|-22.92535|-43.17336|214.00|4|69|1.86|2025-03-06|273|1
RJ|BRL|1374877714740307548|Aconchegante Apto no Flamengo|Norma||Flamengo|Entire home/apt|-22.93875|-43.17825|218.00|1|0|0.00||357|1
RJ|BRL|42278894|Apartment at Flamengo`s District !!!!|Cesar||Flamengo|Entire home/apt|-22.93860|-43.17703|285.00|5|26|0.50|2025-03-06|342|1
RJ|BRL|31057833|Apartamento Boutique perto de Cartão Postal do Rio|Maria||Flamengo|Entire home/apt|-22.94036|-43.17504|436.00|1|36|0.77|2025-03-09|45|8
RJ|BRL|1215356060182748809|Cantinho dos Aráujo2|Geisa||Freguesia (Jacarepaguá)|Private room|-22.94561|-43.35489|175.00|1|2|0.32|2024-09-21|90|4
RJ|BRL|708249745252294421|Apartamento lindo no centro do Rio de Janeiro.|Margareth||Gamboa|Entire home/apt|-22.89579|-43.19562|239.00|4|12|0.39|2025-01-05|255|2
RJ|BRL|1303663832130534437|Quarto: casal, rua residencial perto metro Gloria|Mai||Glória|Private room|-22.92002|-43.18001|180.00|1|0|0.00||351|2
RJ|BRL|1364800375286508673|Lindo e completo apto na Glória|Victor||Glória|Entire home/apt|-22.92155|-43.17433|361.00|2|0|0.00||354|5
RJ|BRL|12969383|APARTAMENTO GRAJAÚ-RJ, RIO2016|Valterson||Grajaú|Entire home/apt|-22.92777|-43.26448|250.00|7|0|0.00||365|1
RJ|BRL|995563517339709401|Espaço Grajaú - Quarto 1|Wagner||Grajaú|Private room|-22.92278|-43.25702|267.00|1|2|0.12|2025-03-04|266|9
RJ|BRL|1359394034371419791|Pousada 422 em Mato Alto Guaratiba|Myriam||Guaratiba|Private room|-22.97901|-43.58414|216.00|1|0|0.00||365|3
RJ|BRL|20303558|Casarão com Piscinas e Campo de Futebol e Vôlei|Jose||Guaratiba|Entire home/apt|-22.98381|-43.54803|980.00|2|32|0.58|2025-02-02|275|4
RJ|BRL|1334828629493489094|Gávea Sunset Residence Hotel-Suíte Vista da Cidade|Andre||Gávea|Private room|-22.98555|-43.24327|509.00|1|0|0.00||364|7
RJ|BRL|2051468|Casa especial com piscina|Guilherme||Gávea|Entire home/apt|-22.98186|-43.24637|3171.00|2|0|0.00||358|1
RJ|BRL|13173748|Quarto casal, 15mn praia, terraço. Fran/Port/Ingl.|Jean Christophe||Humaitá|Private room|-22.95762|-43.19810|122.00|2|13|0.12|2024-01-03|190|1
RJ|BRL|821180038333824821|Apê inteiro - melhor lugar da Zona Sul!|Naiara||Humaitá|Entire home/apt|-22.95625|-43.20048|789.00|2|1|1.00|2025-03-05|177|2
RJ|BRL|40862162|REALY NICE ROOM, AT ARPOADOR|Carol||Ipanema|Private room|-22.98405|-43.19444|247.00|2|28|0.79|2025-01-05|335|3
RJ|BRL|1108042558681148143|Modern 2BDRs 350m from Ipanema Beach / BT82/301|Omar Do Rio||Ipanema|Entire home/apt|-22.98391|-43.19959|362.00|1|23|1.91|2025-03-12|314|214
RJ|BRL|1031223547380096829|Cozy 2bed one block from Ipanema Beach|Clara||Ipanema|Entire home/apt|-22.98612|-43.20695|375.00|3|4|1.03|2025-03-05|162|1
RJ|BRL|14419803|Great View of Rio with Home-Office|Roberto K.||Ipanema|Entire home/apt|-22.98191|-43.19893|396.00|28|12|0.16|2024-11-09|138|2
RJ|BRL|50307577|Flat Arpoador com Garagem e Ótima Localização.|Alessandra||Ipanema|Entire home/apt|-22.98842|-43.19135|404.00|2|106|2.33|2025-02-26|74|23
RJ|BRL|1220456811231531610|Ótimo apartamento em Ipanema A.C 40/1003|Rodrigo||Ipanema|Entire home/apt|-22.98207|-43.19869|430.00|1|16|3.50|2025-03-13|148|79
RJ|BRL|11637570|Apartment Ipanema|Marcio||Ipanema|Entire home/apt|-22.98324|-43.20949|475.00|3|13|0.13|2023-03-29|0|3
RJ|BRL|1348703862610475687|Apt luxo com serviços e estacionamento em Ipanema|Allogio||Ipanema|Entire home/apt|-22.98471|-43.19424|489.00|2|0|0.00||113|18
RJ|BRL|1356591366684213457|Cama Dorm Misto c/Ar em Ipanema|Mariana||Ipanema|Shared room|-22.98310|-43.20874|500.00|1|0|0.00||260|4
RJ|BRL|1320530264292130476|Quarto para 2 em Ipanema|Antonio Mariano||Ipanema|Private room|-22.98575|-43.19457|514.00|1|1|1.00|2025-03-03|364|2
RJ|BRL|4548364|Perfect for a couple - Trendy Ipanema beach|Moema Muller||Ipanema|Entire home/apt|-22.98597|-43.19453|589.00|2|104|0.85|2025-03-16|22|1
RJ|BRL|1324574191083489322|Apartamento Ipanema Vista Linda!|Carlos André||Ipanema|Entire home/apt|-22.98123|-43.20025|800.00|1|0|0.00||363|1
RJ|BRL|15641498|Ocean View Amazing Apartment in Ipanema - 601|Renata||Ipanema|Entire home/apt|-22.98618|-43.20974|900.00|4|1|0.01|2019-09-09|19|4
RJ|BRL|52113196|Flat, Vista Mar,Ponto Nobre,Piscina,Sauna,Jacuzzi.|Melissa||Ipanema|Entire home/apt|-22.98444|-43.21027|1114.00|3|136|3.48|2025-03-17|171|1
RJ|BRL|1312533460824124467|Sofisticação em Ipanema 120 m.|Nelson Gagliano De||Ipanema|Entire home/apt|-22.98222|-43.21374|2000.00|3|1|1.00|2025-03-05|364|1
RJ|BRL|2791048|Beautiful Place in Ipanema|Giovanna||Ipanema|Entire home/apt|-22.98427|-43.20345|3500.00|7|5|0.04|2016-08-17|365|11
RJ|BRL|825322097782497983|Apto em condomínio com garagem proximo BRT/METRÔ|Geovani||Irajá|Entire home/apt|-22.85394|-43.32280|199.00|2|50|1.99|2025-03-04|345|1
RJ|BRL|1070238932750568154|A Casa do Rastaman|Luiz Claudio||Itanhangá|Private room|-22.98920|-43.29742|72.00|2|1|0.10|2024-06-05|105|1
RJ|BRL|1354312177056713323|Itanhangá 5 suítes|Ricardo||Itanhangá|Entire home/apt|-22.98554|-43.30648|15344.00|1|0|0.00||365|23
RJ|BRL|1364842278441107490|mulheres ou casal perto da barra|Paulo Junior||Jacarepaguá|Private room|-22.95175|-43.37744|124.00|2|1|1.00|2025-03-03|90|3
RJ|BRL|1226455878371139792|Riocentro 5 min by foot Barra Beaches 10 min car|José Carlos||Jacarepaguá|Entire home/apt|-22.97354|-43.41383|180.00|1|8|1.27|2025-02-07|300|1
RJ|BRL|1198699510469796970|Residencial com Restaurante a 5 min do Riocentro|Luiz Antonio||Jacarepaguá|Entire home/apt|-22.96365|-43.40043|198.00|2|1|0.16|2024-09-15|269|1
RJ|BRL|38722833|Lindo apto na Barra Olímpica, linda piscina, lazer|Otavio||Jacarepaguá|Entire home/apt|-22.96824|-43.39735|202.00|3|35|0.53|2025-01-31|72|1
RJ|BRL|22324494|MELHOR LOCALIZAÇÃO NA BARRA DA TIJUCA- Apartamento com Ar, wifi, micro|Beatriz||Jacarepaguá|Entire home/apt|-22.96806|-43.38700|203.00|2|59|0.92|2025-03-02|189|40
RJ|BRL|871601413713815293|Suíte fabulosa Flat Barra RJ|Luisa||Jacarepaguá|Private room|-22.96966|-43.36582|282.00|2|12|0.54|2024-08-26|80|2
RJ|BRL|1248054281945906568|Suíte c/ banho até 3 pessoas shows Barra Olímpica|Vitória||Jacarepaguá|Private room|-22.97050|-43.38668|384.00|2|0|0.00||1|3
RJ|BRL|54196848|Barra apartament- conforto e elegância|Renato||Jacarepaguá|Entire home/apt|-22.97004|-43.39117|750.00|1|1|0.04|2023-04-02|365|156
RJ|BRL|14298723|Apt near by Olympic Complex - Barra da Tijuca|Edlea||Jacarepaguá|Entire home/apt|-22.97205|-43.39068|800.00|21|0|0.00||365|1
RJ|BRL|1377640|Exclusive 3beds by the Olympic park|Ana Paula||Jacarepaguá|Entire home/apt|-22.97259|-43.36454|850.00|7|3|0.02|2016-08-12|292|1
RJ|BRL|12978149|Reserva do Parque - Cidade Jardim|Mônica||Jacarepaguá|Entire home/apt|-22.96437|-43.38391|1369.00|10|0|0.00||83|1
RJ|BRL|10319749|Perfect place close olimpic games|Leonardo||Jacarepaguá|Entire home/apt|-22.96876|-43.38742|2266.00|1|0|0.00||365|1
RJ|BRL|1324710008650832709|3 quartos no Jardim Botânico|Elizabeth||Jardim Botânico|Entire home/apt|-22.96464|-43.22290|977.00|2|1|1.00|2025-03-05|364|1
RJ|BRL|36072184|República Femenina Lírios do Vale|Iracy||Jardim Carioca|Entire home/apt|-22.81101|-43.18969|60.00|120|0|0.00||177|1
RJ|BRL|13323580|Vista panaramica /paradisíaco|Mary||Joá|Private room|-23.00272|-43.28024|511.00|1|0|0.00||365|2
RJ|BRL|1317577161817798804|Maison top Joá|Cláudia||Joá|Entire home/apt|-23.00952|-43.28936|15000.00|2|0|0.00||351|7
RJ|BRL|1298639463766194708|Suíte aconchegante em casa linda|Rosalia||Lagoa|Private room|-22.96181|-43.20019|304.00|2|1|1.00|2025-03-05|349|5
RJ|BRL|1349997862042512428|Apto Clássico na Lagoa 3 quartos|Gabriel||Lagoa|Entire home/apt|-22.97035|-43.20505|903.00|2|0|0.00||82|1
RJ|BRL|919694077291090301|Lindo quarto em bairro central, Largo do Machado|Janine||Laranjeiras|Private room|-22.93156|-43.17940|193.00|4|9|0.61|2025-03-05|140|1
RJ|BRL|630178870010517979|Apartamento encantador em Laranjeiras|Vinicius||Laranjeiras|Entire home/apt|-22.93011|-43.18215|210.00|4|32|1.03|2025-02-19|302|1
RJ|BRL|10678925|A quiet bedroom suite, surrounded by greens.|Zilda||Laranjeiras|Private room|-22.93002|-43.18288|283.00|2|41|0.39|2025-03-09|74|1
RJ|BRL|991139797889308020|Luxury apartment aterro flamengo|Danilo||Laranjeiras|Entire home/apt|-22.93534|-43.18113|393.00|3|9|0.51|2025-03-05|125|1
RJ|BRL|52719746|Apartamento no coração do Leblon|Izabella||Leblon|Entire home/apt|-22.98157|-43.22249|214.00|3|29|0.70|2025-02-24|111|1
RJ|BRL|11641219|Leblon... Feels like home! ;-) Welcome Rio|Pollyana||Leblon|Entire home/apt|-22.98077|-43.22421|270.00|2|5|0.08|2020-03-12|8|1
RJ|BRL|26081807|1BR Best Location Leblon-2 Blk from the beach|Claudia||Leblon|Entire home/apt|-22.98536|-43.22691|347.00|5|183|2.27|2025-02-12|11|3
RJ|BRL|974051018811692671|Suíte em ap. tipo casa no Leblon|Virginia||Leblon|Private room|-22.98276|-43.23048|441.00|4|2|0.12|2023-11-21|48|33
RJ|BRL|1361129269214831867|Cool apartment in Leblon for 4 people|RioHost||Leblon|Entire home/apt|-22.98164|-43.21799|544.00|2|1|1.00|2025-03-09|106|44
RJ|BRL|22709688|Rio de Janeiro Leblon 5 pessoas LB204B|Giovanni||Leblon|Entire home/apt|-22.98401|-43.21882|767.00|2|109|1.27|2025-03-17|228|69
RJ|BRL|1326031352854488133|Leblon sea&lake view|Edicleis||Leblon|Entire home/apt|-22.98262|-43.21746|933.00|1|2|1.28|2025-02-08|39|1
RJ|BRL|2930509|Classic Penthouse Leblon beach|Gilson||Leblon|Entire home/apt|-22.98371|-43.21726|969.00|9|35|0.27|2025-03-07|77|3
RJ|BRL|13766448|APART HOTEL 2 bedrooms View of the Sea and Christ|Gessy||Leblon|Entire home/apt|-22.98404|-43.22124|1103.00|4|5|0.05|2019-10-06|5|2
RJ|BRL|1239015180884731652|Apartamento lindo/ aconchegante|Charles||Leblon|Entire home/apt|-22.98185|-43.22637|1284.00|1|3|0.73|2025-03-04|364|2
RJ|BRL|13276258|Qt e suíte na praia do Leblon|Carlos Henrique||Leblon|Entire home/apt|-22.98631|-43.22194|5664.00|25|0|0.00||88|1
RJ|BRL|992635472722840562|Copacabana em frente à praia Leme sofá2|Monica||Leme|Shared room|-22.96247|-43.16741|164.00|1|33|1.98|2025-03-11|268|28
RJ|BRL|785598638838983895|NOVO! Muito charme e conforto a 1 quadra da praia|Yuri||Leme|Entire home/apt|-22.96318|-43.17068|270.00|2|84|3.17|2025-03-05|226|44
RJ|BRL|560771466115336221|Du Leme: 3 qts (1st) e vaga p/ carro em Copacabana|Newton||Leme|Entire home/apt|-22.96188|-43.16746|300.00|1|76|2.07|2025-02-20|137|65
RJ|BRL|771684112494639129|Na praia com vista mar. Durma ao som do mar.|Samantha||Leme|Entire home/apt|-22.96446|-43.17199|308.00|3|27|0.98|2024-02-23|263|5
RJ|BRL|1231766733734420935|2 quartos Leme pertinho da Praia|Morada||Leme|Entire home/apt|-22.96316|-43.17104|319.00|1|20|3.37|2025-02-26|250|14
RJ|BRL|1309330137195597877|Varandão com vista para o mar|Livia||Leme|Private room|-22.96324|-43.17249|320.00|2|2|1.67|2025-02-23|152|1
RJ|BRL|1137923826135699686|Conforto, 2 min praia, quartos c/ ar, 2 banheiros|Nilia||Leme|Entire home/apt|-22.96293|-43.17045|451.00|3|8|0.82|2025-02-12|20|1
RJ|BRL|1237836681946899509|Apartamento varanda vista verde|Marcia||Lins de Vasconcelos|Entire home/apt|-22.90948|-43.28025|144.00|1|0|0.00||71|1
RJ|BRL|1366618172663638435|Quarto de casal na Tijuca|Dhavid||Maracanã|Private room|-22.91217|-43.21907|228.00|1|2|2.00|2025-03-06|365|1
RJ|BRL|1346281303040125364|Apart no Maracanã próximo metrô|Saulo||Maracanã|Entire home/apt|-22.91468|-43.23186|360.00|1|1|1.00|2025-03-05|364|1
RJ|BRL|700156647569154582|Quitinete independente em vila.|Leonardo||Marechal Hermes|Entire home/apt|-22.86970|-43.37120|104.00|2|49|2.04|2025-03-08|351|1
RJ|BRL|1002144829952543105|Evrthg close!30min playas,20min Lapa or Sambodromo|Flávia||Méier|Shared room|-22.90097|-43.27570|67.00|1|5|0.29|2025-02-20|164|14
RJ|BRL|1330477830654594888|Apartamento no coração da ZN|Leandra||Méier|Entire home/apt|-22.90548|-43.27678|153.00|1|10|4.41|2025-02-28|352|1
RJ|BRL|1007456218278044427|Casa próx a barra da Tijuca e Copacabana|Emyli||Osvaldo Cruz|Entire home/apt|-22.86163|-43.35183|320.00|5|5|0.30|2024-01-02|364|5
RJ|BRL|818383367237123766|ótima casa para o carnaval|André Martins De Souza||Paciência|Entire home/apt|-22.91099|-43.62912|1999.00|1|0|0.00||365|1
RJ|BRL|1056035466814922675|A Moreninha|Cris||Paquetá|Entire home/apt|-22.75602|-43.11035|350.00|2|15|1.09|2025-02-06|282|1
RJ|BRL|797109502369526935|Apartamento zona norte RJ|Robson||Parque Anchieta|Entire home/apt|-22.83593|-43.40120|140.00|1|0|0.00||281|3
RJ|BRL|1325633645425079492|Apartamento no Rio de Janeiro|Ju||Pavuna|Entire home/apt|-22.81515|-43.38447|193.00|2|1|0.41|2025-01-06|90|1
RJ|BRL|670263384885687849|Suíte com cama de solteiro|Marina||Pechincha|Private room|-22.92821|-43.35808|68.00|1|19|0.61|2025-01-02|46|1
RJ|BRL|1343646282849835815|Apto com garagem - Ciaa, Cefam, Ares Marinheiro|Priscilla||Penha|Entire home/apt|-22.82912|-43.27649|147.00|3|2|2.00|2025-03-04|86|4
RJ|BRL|2882788|Apartamento aconchegante|Jackson||Penha Circular|Entire home/apt|-22.84199|-43.29682|250.00|1|1|0.01|2016-01-07|365|1
RJ|BRL|1079666247081087765|Kitnet (Perto do Estádio)|Rafaela||Piedade|Entire home/apt|-22.88615|-43.31089|110.00|3|11|0.88|2025-03-05|212|1
RJ|BRL|666545504003384593|Quarto 03 Para 1 Pessoas - Perto do Aeroporto GIG|More Só||Portuguesa|Private room|-22.80483|-43.21007|199.00|1|2|0.07|2024-02-11|272|18
RJ|BRL|25399129|Apartment Ilha Plaza (próximo ao Aeroporto Galeão)|Carmen||Portuguesa|Entire home/apt|-22.80058|-43.20219|19000.00|7|12|0.15|2020-01-01|90|1
RJ|BRL|14488323|Olympic Games 2016|Stefanie||Praça Seca|Private room|-22.89271|-43.34740|99.00|1|16|0.21|2024-09-15|89|1
RJ|BRL|1024042178762071760|Apartamento 3quartos próximo BRT|Tiago||Praça Seca|Entire home/apt|-22.89200|-43.34348|191.00|1|2|0.14|2024-09-21|167|4
RJ|BRL|980908833034687901|Apartamento Quarto e Sala|Alison||Praça da Bandeira|Entire home/apt|-22.91386|-43.21344|80.00|2|4|0.24|2024-11-23|251|1
RJ|BRL|1377611287377397738|Suite para casal 301 com ar|Sebastião||Praça da Bandeira|Private room|-22.91306|-43.21270|232.00|1|0|0.00||365|16
RJ|BRL|1287158037911669184|Apt acesso á praia da Macumba Recreio|Renata||Recreio dos Bandeirantes|Entire home/apt|-23.03062|-43.48220|252.00|5|5|1.81|2025-03-11|352|1
RJ|BRL|933403306516190022|3 Qts - Cond. com Estacionamento e Segurança 24hs|Fabio||Recreio dos Bandeirantes|Entire home/apt|-23.00604|-43.45683|261.00|3|36|2.13|2025-03-06|56|1
RJ|BRL|1082353690491412872|Rústico & Black 303|Kharisma||Recreio dos Bandeirantes|Entire home/apt|-23.00286|-43.44201|275.00|1|40|2.99|2025-03-11|187|9
RJ|BRL|960067747307777768|50 mt da Praia do Recreio, Cobertura Vista Mar.|Bianca||Recreio dos Bandeirantes|Entire home/apt|-23.02997|-43.47400|300.00|2|7|0.42|2025-03-03|88|1
RJ|BRL|1019579511149924620|Apartamento 2 Qto e Varanda em Condomínio Completo|Atrium||Recreio dos Bandeirantes|Entire home/apt|-23.02430|-43.50951|302.00|2|17|1.05|2025-03-05|329|13
RJ|BRL|1051668853607967366|Lindo e Novo no Recreio !|Adilson||Recreio dos Bandeirantes|Entire home/apt|-23.01836|-43.48216|309.00|3|13|0.88|2025-03-04|256|1
RJ|BRL|1140060531104437973|Casa de praia no Recreio|Felipe||Recreio dos Bandeirantes|Entire home/apt|-23.03185|-43.48244|329.00|1|2|0.32|2024-09-22|266|9
RJ|BRL|983242756784693176|PÉ NA AREIA! Férias de Julho com Desconto!|Carla||Recreio dos Bandeirantes|Entire home/apt|-23.02250|-43.51004|350.00|2|6|0.37|2024-09-22|170|1
RJ|BRL|32779465|Cobertura em Resort no Recreio dos Bandeirantes|Ana Lucia||Recreio dos Bandeirantes|Entire home/apt|-23.00633|-43.48525|540.00|5|0|0.00||31|1
RJ|BRL|1356833478246025186|Apto 200m praia 3 quartos|Renata||Recreio dos Bandeirantes|Entire home/apt|-23.02456|-43.46122|818.00|2|1|1.00|2025-03-04|365|1
RJ|BRL|1056781478284621457|Apart Mobiliado próx. a praia|Higor||Recreio dos Bandeirantes|Entire home/apt|-23.02809|-43.47331|2500.00|1|1|0.07|2024-01-07|270|1
RJ|BRL|1356493763231173529|Aconchegante pouso no coração do Rio|Drica||Riachuelo|Entire home/apt|-22.90329|-43.25309|180.00|2|2|2.00|2025-03-05|346|1
RJ|BRL|1318715136727840355|Céntrico. Carnaval. Copacabana. Para mujeres. JGC3|Nathalia||Rio Comprido|Private room|-22.92738|-43.21176|141.00|1|0|0.00||365|38
RJ|BRL|1367505159023383165|Hostel casa grande|Mey||Rio Comprido|Shared room|-22.92749|-43.20939|149.00|1|1|1.00|2025-03-04|365|4
RJ|BRL|1295138323456653922|AP Confortável e bem localizado|Rosane||Rio Comprido|Entire home/apt|-22.92353|-43.20931|272.00|3|0|0.00||360|2
RJ|BRL|1369478371342267293|Rocinha|Leandro||Rocinha|Entire home/apt|-22.98677|-43.25217|141.00|3|1|1.00|2025-03-09|236|1
RJ|BRL|22092074|Apartamento Completo e seguro, Santa Cruz-RJ|Reginaldo||Santa Cruz|Entire home/apt|-22.89925|-43.67792|99.00|1|117|1.33|2025-03-04|339|1
RJ|BRL|14535489|Beds in Rio - Here you feel at home!|Filipi E Jessica||Santa Teresa|Shared room|-22.91743|-43.17969|61.00|1|11|0.11|2025-01-06|337|8
RJ|BRL|1334187590442331307|Quarto com vista verde|Bruna||Santa Teresa|Private room|-22.92292|-43.19006|156.00|2|4|2.86|2025-03-09|353|1
RJ|BRL|1299209877115150814|Suíte 1|Marcelo||Santa Teresa|Private room|-22.92900|-43.19549|247.00|1|2|0.80|2025-01-08|100|6
RJ|BRL|16794421|Apartamento aconchegante no coração da Gloria!|Fernando||Santa Teresa|Entire home/apt|-22.91796|-43.17833|250.00|15|17|0.17|2024-01-06|332|1
RJ|BRL|1104503825710439489|Apartamento Lapa Quarto & Sala !|Christine||Santa Teresa|Entire home/apt|-22.91591|-43.18563|250.00|1|0|0.00||269|1
RJ|BRL|4820924|Lugar calmo e silencioso!|José Antonio||Santa Teresa|Private room|-22.92578|-43.18968|315.00|3|6|0.24|2025-03-07|365|3
RJ|BRL|22366388|fenix|Antonio||Santa Teresa|Private room|-22.91851|-43.17893|669.00|6|0|0.00||358|1
RJ|BRL|1311722184292889633|Acorde com passarinhos, há dez minutos da Lapa.|Maria Inês||Santa Teresa|Entire home/apt|-22.91839|-43.17909|1045.00|1|1|0.39|2025-01-03|77|1
RJ|BRL|32135423|QUARTO DUPLO 3 HOSTEL MOTA|Ilson||Santo Cristo|Private room|-22.90170|-43.20154|252.00|1|0|0.00||72|3
RJ|BRL|867419268836472410|Ap show mobília central seguro|Renata Esther||Saúde|Entire home/apt|-22.89856|-43.18184|96.00|6|0|0.00||356|1
RJ|BRL|1277804520415102962|Quarto Revelion Centro do Rio RJ|Camila||Saúde|Private room|-22.89787|-43.18588|214.00|1|0|0.00||178|4
RJ|BRL|1045001087803600026|Vista do por do sol e montanhas|Teresa||São Conrado|Private room|-22.99276|-43.25444|280.00|2|0|0.00||269|3
RJ|BRL|1288026758933994903|Hotel Nacional RJ-Vista montanha|Daniel||São Conrado|Private room|-22.99842|-43.25770|947.00|4|1|1.00|2025-03-08|133|7
RJ|BRL|1030656131698686580|Cobertura Duplex VistaMar, Piscina & Churrasqueira|Vinicius||São Conrado|Entire home/apt|-22.99823|-43.25730|1143.00|1|3|0.22|2025-01-28|269|5
RJ|BRL|23475892|HOUSE OF LIGHT|Ricardo||São Conrado|Entire home/apt|-23.00067|-43.27633|17500.00|2|0|0.00||365|42
RJ|BRL|53372651|Quarto 4 aconchegante 3pessoas São Cristóvão|Diogo||São Cristóvão|Private room|-22.90239|-43.22341|75.00|1|39|0.96|2025-03-11|362|8
RJ|BRL|1356685942513389847|Metrô, Zoológico Maracanã, Praia|Diego||São Cristóvão|Entire home/apt|-22.90376|-43.21885|180.00|2|0|0.00||107|2
RJ|BRL|1039092035030153536|Espaço Inteiro ao lado da UERJ e Maracanã|Caio||São Francisco Xavier|Entire home/apt|-22.90603|-43.24268|180.00|1|46|3.18|2025-03-10|248|1
RJ|BRL|34561663|Hospedagem em JACAREPAGUÁ - HOSTEL MATTOS 2|Greice||Taquara|Private room|-22.92576|-43.37768|135.00|1|6|0.16|2024-09-16|364|2
RJ|BRL|42641719|Projac apt 507 Samba Rio|Valeria||Taquara|Entire home/apt|-22.93614|-43.37190|178.00|1|26|0.56|2025-03-02|360|2
RJ|BRL|1242016087599070597|Ótimo Apto 1 quarto mobiliado|Victor||Tauá|Entire home/apt|-22.79954|-43.18795|125.00|7|4|0.90|2025-03-13|355|4
RJ|BRL|1325464561874392294|Quarto em área nobre da Tijuca|Gislaine||Tijuca|Private room|-22.93106|-43.24840|86.00|1|3|1.27|2025-03-05|348|1
RJ|BRL|747263734968863511|Quarto Amplo Exclusivo na Tijuca - Metrô pertinho|Ronen||Tijuca|Private room|-22.92423|-43.22201|108.00|7|13|0.48|2025-01-02|175|3
RJ|BRL|1065910214836421570|Excelente Quarto e Sala|Jacir||Tijuca|Entire home/apt|-22.92602|-43.22659|288.00|2|3|0.22|2025-03-05|360|1
RJ|BRL|2543902|Excellent Apartment noble Tijuca!|Maria Clara||Tijuca|Entire home/apt|-22.93298|-43.24706|1500.00|15|0|0.00||365|1
RJ|BRL|6165607|2 Suítes no Rio|Marcela||Todos os Santos|Entire home/apt|-22.89984|-43.28639|154.00|2|5|0.74|2025-02-17|331|2
RJ|BRL|1148105251744312899|Quartos Rock in rio|Ed||Todos os Santos|Private room|-22.89767|-43.28841|1421.00|1|0|0.00||263|1
RJ|BRL|1043837475470438637|Urca Paraiso 4 pessoas apto inteiro|Ana||Urca|Entire home/apt|-22.94796|-43.16308|1395.00|3|0|0.00||269|5
RJ|BRL|1081132111304321243|Charmoso loft próximo ao Recreio Shopping|Americas||Vargem Grande|Entire home/apt|-23.02081|-43.49562|184.00|28|6|0.51|2025-03-05|269|2
RJ|BRL|733427110674579340|A Vibe de Vargem - Suíte Grumari|Marcelo||Vargem Grande|Private room|-22.99329|-43.50652|498.00|2|0|0.00||87|2
RJ|BRL|1045098443851161819|Charmoso e confortável quarto|Joelma||Vargem Pequena|Private room|-22.98628|-43.46732|84.00|1|6|0.47|2025-01-01|197|3
RJ|BRL|1374580347086610322|Linda casa prox RioCentro, Farmasi, Parq Olímpico|Lucas||Vargem Pequena|Entire home/apt|-22.98853|-43.46558|567.00|1|1|1.00|2025-03-14|353|5
RJ|BRL|1366596347002518525|quarto em apartamento, piscina|Paulo Henrique De Araújo Amaro||Vasco da Gama|Private room|-22.88910|-43.22701|99.00|1|0|0.00||365|1
RJ|BRL|48282753|Stay & Assist at our School & Gain a TEFL|Larry||Vidigal|Shared room|-22.99498|-43.23720|64.00|4|9|0.35|2025-02-07|137|10
RJ|BRL|984914648490130890|Quarto no alto do Vidigal.|Isabela||Vidigal|Private room|-22.99597|-43.24481|134.00|1|5|0.29|2025-01-04|269|1
RJ|BRL|1314128806516965262|Vidigal Doce Lar|Igor||Vidigal|Entire home/apt|-22.99530|-43.24021|227.00|1|1|1.00|2025-03-05|177|1
RJ|BRL|41982581|Casa em vila Isabel/RJ carnaval 7 dias|Anna Paula||Vila Isabel|Entire home/apt|-22.91162|-43.24000|67.00|7|0|0.00||365|1
RJ|BRL|1322828047545804046|Quarto para mulheres - UERJ/Pedro Ernesto/Maracanã|Ana Helena||Vila Isabel|Private room|-22.91581|-43.24628|100.00|2|2|2.00|2025-03-04|40|1
RJ|BRL|1212591377699772239|Casa triplex em Vila Valqueire.|Carlos Augusto Santiago Maciel||Vila Valqueire|Entire home/apt|-22.88421|-43.37313|522.00|1|0|0.00||365|1"""
}
