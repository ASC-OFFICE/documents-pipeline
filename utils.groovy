def checkoutRepo(String repo, String branch = 'develop', String company = 'ONLYOFFICE') {
    checkout([
            $class: 'GitSCM',
            branches: [[
                    name: "develop"
                ]
            ],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[
                    $class: 'RelativeTargetDirectory',
                    relativeTargetDir: repo
                ]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [[
                    url: "git@github.com:${company}/${repo}.git"
                ]
            ]
        ]
    )
}

return this

def getReposList()
{
    def repos = []
    repos.add('build_tools')
    repos.add('core')
    repos.add('desktop-apps')
    repos.add('desktop-apps-ext')
    repos.add('desktop-sdk')
    repos.add('dictionaries')
    repos.add('document-builder-package')
    repos.add('r7')
    repos.add('sdkjs')
    repos.add('sdkjs-plugins')
    repos.add('web-apps')
    repos.add('DocumentBuilder')
    return repos
}

def checkoutRepos(String branch = 'develop')
{    
    for (repo in getReposList()) {
        if( repo != 'r7' ) {
            checkoutRepo(repo, branch)
        }
        else {
            checkoutRepo(repo, branch, 'ASC-OFFICE')
        }
    }

    return this
}

def tagRepos(String tag)
{
    for (repo in getReposList()) {
        sh "cd ${repo} && \
            git tag -l | xargs git tag -d && \
            git fetch --tags && \
            git tag ${tag} && \
	        git push origin --tags"
    }

    return this
}

def linuxBuild(String branch = 'develop', String platform = 'native', Boolean clean = true)
{
    checkoutRepos(branch)
    sh "cd build_tools && \
        ./configure.py \
            --module \"desktop builder core\"\
            --platform ${platform}\
            --update false\
            --branch develop\
            --clean ${clean.toString()}\
            --qt-dir \$QT_PATH &&\
        ./make.py"
    /*
    sh "cd desktop-apps/win-linux/package/linux &&\
         make clean &&\
         make deploy"
    */
    sh "cd document-builder-package &&\
         make deploy"
    /*
    sh "cd core && \
        make deploy"
    publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            includes: 'index.html',
            keepAll: true,
            reportDir: 'desktop-apps-ext/win-linux/package/linux',
            reportFiles: 'index.html',
            reportName: "DesktopEditors(${platform})",
            reportTitles: ''
        ]
    )
    */
    publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            includes: 'index.html',
            keepAll: true,
            reportDir: 'document-builder-package',
            reportFiles: 'index.html',
            reportName: "DocumentBuilder(${platform})",
            reportTitles: ''
        ]
    )
    /*
    checkoutRepo('doc-builder-testing')
    sh "docker rmi doc-builder-testing || true"
    sh "cd doc-builder-testing &&\
        docker build --tag doc-builder-testing -f dockerfiles/debian-develop/Dockerfile . &&\
        docker run --rm doc-builder-testing parallel_rspec spec -n 2"
    */
    return this
}

/*
def windowsBuild(String branch = 'master', String platform = 'native', Boolean clean = true)
{
    checkoutRepos(branch)

    bat "cd build_tools &&\
            call python configure.py\
            --module \"desktop builder core tests updmodule\"\
            --platform ${platform}\
            --update false\
            --branch ${branch}\
            --branding r7\
            --clean ${clean.toString()}\
            --qt-dir \"C:\\Qt\\Qt5.9.8\\5.9.8\"\
            --qt-dir-xp \"C:\\Qt\\Qt5.6.3\\5.6.3\" &&\
            call python make.py"

    bat "cd desktop-apps-ext &&\
            mingw32-make clean-package &&\
            mingw32-make deploy"

    publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            includes: 'index.html',
            keepAll: true,
            reportDir: 'desktop-apps-ext/win-linux/package/windows',
            reportFiles: 'index.html',
            reportName: "DesktopEditors(${platform})",
            reportTitles: ''
        ]
    )

    if ( !platform.endsWith('_xp') ) {
        bat "cd document-builder-package &&\
            mingw32-make clean &&\
            mingw32-make deploy"

        String winSdkVersion = '10.0.14393.0'
        String platformType
        
        switch (platform) {
            case 'win_64':
                platformType = 'x64'
                break
            case 'win_32':
                platformType = 'x86'
                break
            default:
                platformType = ''
        }

        bat "cd core && \
            call \"C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\vcvarsall.bat\" ${platformType} ${winSdkVersion} && \
            mingw32-make deploy"

        publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: false,
                includes: 'index.html',
                keepAll: true,
                reportDir: 'document-builder-package',
                reportFiles: 'index.html',
                reportName: "DocumentBuilder(${platform})",
                reportTitles: ''
            ]
        )
    }

}
*/
