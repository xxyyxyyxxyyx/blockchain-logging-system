#!/bin/sh
rm -rf config/*
rm -rf crypto-config/*

mkdir -p config
mkdir -p crypto-config
echo "--- Generating certificates ---"
cryptogen generate --config=./crypto-config.yaml
echo "--- Creating genesis block ---"
configtxgen -profile OneOrgOrdererGenesis -outputBlock ./config/genesis.block
echo "--- Generating channel configuration ---"
configtxgen -profile OneOrgChannel -outputCreateChannelTx ./config/channel.tx -channelID mychannel
echo "--- Generating anchor peers ---"
configtxgen -profile OneOrgChannel -outputAnchorPeersUpdate ./config/Org1MSPanchors.tx -channelID mychannel -asOrg Org1MSP

