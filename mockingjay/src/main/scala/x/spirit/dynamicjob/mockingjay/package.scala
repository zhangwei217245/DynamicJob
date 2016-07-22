package x.spirit.dynamicjob

/**
  * Created by zhangwei on 7/19/16.
  */
package object mockingjay {

  var Red = Set[String]("romney", "@mittromney", "@reppaulryan", "@ronpaul", "@newtgingrich", "@ricksantorum",
    "#romney", "#mittromney", "#tcot", "#tlot", "#ocra", "#gop", "#gop2012", "#rnc", "#romney2012",
    "#republicans", "#republican", "#teaparty", "#right", "#sgp", "#hhrs", "#rush", "#secede", "#conservative",
    "#rep", "#voterep", "#socialism", "#socialist", "#icon", "#romneyryan2012")//.map(_.replace("@","").replace("#",""));
  //Red.union(Red.map("@".concat(_))).union(Red.map("#".concat(_)))

  var Blue = Set[String]("obama", "@barackobama", "biden", "@joebiden", "@thedemocrats", "#obamacare", "#healthcare", "#ilikeobama",
    "#obama", "#barackobama", "#barackthevote", "#fourmoreyears", "#dnc2012", "#dnc", "#liberal", "#uniteble",
    "#democrats", "#democrat", "#left", "#dems", "#dem", "#votedem", "#p2", "#topprog", "#hcr", "#teamobama",
    "#voteobama", "#4moreyears", "#2terms")//.map(_.replace("@","").replace("#",""));
  //Blue.union(Blue.map("@".concat(_))).union(Blue.map("#".concat(_)))
}
