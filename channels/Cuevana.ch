version=0.2	
channel Cuevana {
   login {
      # Login data
      url=http://www.cuevana.tv/login_get.php
      user=usuario
      passwd=password
      type=cookie
      params=ingresar=true&recordarme=si
      #matcher=cue_id
   }

	folder {
		url=http://www.cuevana.tv/peliculas
		name=Peliculas - Ultimas
		macro=listapelisMacro
		}

	folder {
		url=http://www.cuevana.tv/peliculas/populares/
		name=Peliculas - Mas Populares
		macro=listapelisMacro
		}
	folder {
		url=http://www.cuevana.tv/peliculas/mejorpuntuadas/
		name=Peliculas - Mejor Puntadas
		macro=listapelisMacro
		}
	folder {
		url=http://www.cuevana.tv/peliculas/genero/
		name=Peliculas - Por Genero
		macro=generosMacro
		}
	folder {
		url=http://www.cuevana.tv/series
		name=Series TV
		macro=tvseriesMacro
		}
}

macrodef generosMacro {
	folder {
		matcher=<option value='([^\D]+)'>([^\<]+)<
		order=url,name
		url=http://www.cuevana.tv/peliculas/genero/a=genero&genero=
		macro=listapelisMacro
		}
}		

macrodef mediaMacro {
	media {
		matcher=class='row[^\*]+?img\ssrc='([^\']+)'[^\*]+?href='([^\']+)'>([^\<]+)<
		order=thumb,url,name
		script=cuevanascript
		prop=prepend_url=http://www.cuevana.tv
		}
}

macrodef listapelisMacro {
	
	folder {
		matcher=<li><a href=\"([^\"]+)\" class=\"sel\">
		type=empty
		prop=prepend_parenturl
		macro=mediaMacro
		}

	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
	folder {
		matcher=<li><a href=\"([^\"]+)\" class="sel">[^\*]+?class='next' href='([^\#\']+)'
		order=url,url
		type=empty
		url=http://www.cuevana.tv
		macro=mediaMacro
		
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
macrodef tvseriesMacro {
	folder {
		matcher=serieslist\.push\(\{id:([^\,]+),nombre:\"([^\"]+)\"
		order=url,name
		url=http://www.cuevana.tv/list_search_id.php?serie=
	folder {
		matcher=listSeries\(2,\"([^\"]+?)\"[^\>]+?>([^\<]+?)<
		order=url,name
		url=http://www.cuevana.tv/list_search_id.php?temporada=
	folder {
		matcher=listSeries\(3,\"([^\D]+)\"[^\*]+?nume'>([^\*]*?)</span>([^\<]+?)</li>
		order=url,name,name
		url=http://www.cuevana.tv/list_search_info.php?episodio=
		prop=name_separator= - ,
	media {
		matcher=img src=\"([^\"]+)\"[^\*]+?class=\"tit\">[^\*]+?clearleft\"></div>[^\*]+?>([^\<]+)<[^\*]+?window\.location='([^\']+)'
		order=thumb,name,url
		prop=prepend_url=http://www.cuevana.tv,prepend_thumb=http://www.cuevana.tv
		script=cuevanascript
		}
	}
}
}
}