run() {
                        echo "\$ ${@}"
                        "${@}"
                        res=$?
                        if [ $res != 0 ]; then
                          echo
                          echo "Failed!"
                          echo
                          exit $res
                        fi
                      }

                      run tar cf hadoop-2.4.0.tar hadoop-2.4.0
                      run gzip -f hadoop-2.4.0.tar
                      echo
                      echo "Hadoop dist tar available at: /home/liubo/git/hadoop-2.4.0/hadoop-dist/target/hadoop-2.4.0.tar.gz"
                      echo