---
layout: docs
title: OSMRead
category: h2drivers
is_function: true
description: OSM &rarr; Table
prev_section: SHPWrite
next_section: ST_AsGeoJson
permalink: /docs/dev/OSMRead/
---

### Signatures

{% highlight mysql %}
OSMRead(VARCHAR path);
OSMRead(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

A refaire !!!

Read a OSM file and copy the content in the specified tables.

### Examples

H2GIS extract polygons:

{% highlight mysql %}
drop table if exists MAP_GEOM;
create table buildings(ID_WAY bigint primary key) as SELECT DISTINCT ID_WAY FROM MAP_WAY_TAG WT, MAP_TAG T WHERE WT.ID_TAG = T.ID_TAG AND T.TAG_KEY IN ('building');
create table MAP_WAY_GEOM AS SELECT ID_WAY, ST_MAKEPOLYGON(ST_MAKELINE(THE_GEOM)) THE_GEOM FROM (SELECT (SELECT ST_ACCUM(THE_GEOM) THE_GEOM FROM (SELECT N.ID_NODE, N.THE_GEOM,WN.ID_WAY IDWAY FROM MAP_NODE N,MAP_WAY_NODE" WN WHERE N.ID_NODE = WN.ID_NODE ORDER BY WN.NODE_ORDER) WHERE  IDWAY = W.ID_WAY) THE_GEOM ,W.ID_WAY FROM "MAP_WAY" W,BUILDINGS B WHERE W.ID_WAY = B.ID_WAY) GEOM_TABLE WHERE ST_GEOMETRYN(THE_GEOM,1) = ST_GEOMETRYN(THE_GEOM, ST_NUMGEOMETRIES(THE_GEOM)) AND ST_NUMGEOMETRIES(THE_GEOM) > 2;
{% endhighlight %}

PostGIS extract polygons:
{% highlight mysql %}
drop table if exists MAP_BUILDINGS;
create table MAP_BUILDINGS as SELECT DISTINCT ID_WAY FROM "map_way_tag" WT, "map_tag" T WHERE WT.ID_TAG = T.ID_TAG AND T.TAG_KEY IN ('building');
alter table MAP_BUILDINGS ADD primary key(ID_WAY);
drop table if exists MAP_WAY_GEOM;
create table MAP_WAY_GEOM AS SELECT ID_WAY,
ST_MakePolygon(ST_MAKELINE(THE_GEOM)) THE_GEOM FROM (SELECT (SELECT ST_ACCUM(THE_GEOM) THE_GEOM FROM
 (SELECT N.ID_NODE, N.THE_GEOM,WN.ID_WAY IDWAY FROM "map_node" N,"map_way_node" WN WHERE N.ID_NODE = WN.ID_NODE ORDER BY WN.NODE_ORDER)  ORDEREDNODES WHERE  ORDEREDNODES.IDWAY = W.ID_WAY) THE_GEOM ,W.ID_WAY FROM "map_way" W,MAP_BUILDINGS B WHERE W.ID_WAY = B.ID_WAY) GEOM_TABLE WHERE array_length(THE_GEOM, 1) > 2 and ST_EQUALS(THE_GEOM[1],THE_GEOM[array_length(THE_GEOM, 1)]);
  
 {% endhighlight %}

##### See also

* [`OSMRead`](../OSMRead)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2drivers/src/main/java/org/h2gis/drivers/osm/OSMRead.java" target="_blank">Source code</a>

[wiki]: http://wiki.openstreetmap.org/wiki/OSM_XML
