import { execFileSync } from 'child_process'
import { writeFileSync, unlinkSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const git = 'C:\\Program Files\\Git\\cmd\\git.exe'
const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const msgPath = join(root, 'commit-msg.txt')

const env = {
  ...process.env,
  GIT_AUTHOR_NAME: 'marciodev2026',
  GIT_AUTHOR_EMAIL: 'marcioeb.sistemas2023@gmail.com',
  GIT_COMMITTER_NAME: 'marciodev2026',
  GIT_COMMITTER_EMAIL: 'marcioeb.sistemas2023@gmail.com'
}

const run = (args) => execFileSync(git, args, { cwd: root, encoding: 'utf8', env })

const message = process.argv[2] || 'Update'
writeFileSync(msgPath, message, 'utf8')

run(['add', '-A'])
const tree = run(['write-tree']).trim()
const parent = run(['rev-parse', 'HEAD']).trim()
const newCommit = run(['commit-tree', tree, '-p', parent, '-F', msgPath]).trim()
run(['reset', '--hard', newCommit])
unlinkSync(msgPath)
console.log(newCommit)
