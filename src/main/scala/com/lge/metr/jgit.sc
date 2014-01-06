package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter._

object jgit {
  val path = "/Users/jooyunghan/work/scala/metr/.git/modules/samples/github-android"
                                                  //> path  : String = /Users/jooyunghan/work/scala/metr/.git/modules/samples/gith
                                                  //| ub-android
  val repo: Repository = new FileRepositoryBuilder().setGitDir(new File(path)).findGitDir.build
                                                  //> repo  : org.eclipse.jgit.lib.Repository = Repository[/Users/jooyunghan/work/
                                                  //| scala/metr/.git/modules/samples/github-android]
  val head = repo.getRef("refs/heads/master")     //> head  : org.eclipse.jgit.lib.Ref = Ref[refs/heads/master=c6bdc122f2e8ac3594d
                                                  //| 7b83b998bb06bb7748488]
  val walk = new RevWalk(repo)                    //> walk  : org.eclipse.jgit.revwalk.RevWalk = org.eclipse.jgit.revwalk.RevWalk@
                                                  //| 10bf989e
  walk.parseCommit(head.getObjectId)              //> res0: org.eclipse.jgit.revwalk.RevCommit = commit c6bdc122f2e8ac3594d7b83b99
                                                  //| 8bb06bb7748488 1386790622 -----p
  
  val tree = walk.parseTree(repo.resolve("HEAD")) //> tree  : org.eclipse.jgit.revwalk.RevTree = tree 61beebdb90f7222810643cbfe650
                                                  //| 19406b4125d5 -----p
  val treeWalk = new TreeWalk(repo)               //> treeWalk  : org.eclipse.jgit.treewalk.TreeWalk = org.eclipse.jgit.treewalk.T
                                                  //| reeWalk@642052de

        treeWalk.addTree(tree)                    //> res1: Int = 0
        treeWalk.setRecursive(true)
        treeWalk.setFilter(OrTreeFilter.create(PathFilter.create("app/src"), PathSuffixFilter.create(".java")))
  treeWalk.next                                   //> res2: Boolean = true
  treeWalk.getObjectId(0)                         //> res3: org.eclipse.jgit.lib.ObjectId = AnyObjectId[04540e93e9735bae9993504e12
                                                  //| d6b8fc0cfb580e]
  repo.open(treeWalk.getObjectId(0)).copyTo(System.out)
                                                  //> /*
                                                  //|  * Copyright 2012 GitHub Inc.
                                                  //|  *
                                                  //|  * Licensed under the Apache License, Version 2.0 (the "License");
                                                  //|  * you may not use this file except in compliance with the License.
                                                  //|  * You may obtain a copy of the License at
                                                  //|  *
                                                  //|  *  http://www.apache.org/licenses/LICENSE-2.0
                                                  //|  *
                                                  //|  * Unless required by applicable law or agreed to in writing, software
                                                  //|  * distributed under the License is distributed on an "AS IS" BASIS,
                                                  //|  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                                  //|  * See the License for the specific language governing permissions and
                                                  //|  * limitations under the License.
                                                  //|  */
                                                  //| package com.github.mobile;
                                                  //| 
                                                  //| import static android.os.Build.VERSION.SDK_INT;
                                                  //| import static android.os.Build.VERSION_CODES.FROYO;
                                                  //| 
                                                  //| import com.github.kevinsawicki.http.HttpRequest;
                                                  //| 
                                                  //| import java.net.HttpURLConnection;
                                                  //| 
                                                  //| import org.eclipse.egit.github.core.client.GitHubClient;
                                                  //| 
                                                  //| /**
                                                  //|  * Default client u
                                                  //| Output exceeds cutoff limit.

}